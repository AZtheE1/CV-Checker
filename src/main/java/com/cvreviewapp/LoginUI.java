package com.cvreviewapp;

import com.cvreviewapp.models.User;
import com.cvreviewapp.services.UserService;
import com.cvreviewapp.utils.Constants;
import com.cvreviewapp.utils.UITheme;
import com.cvreviewapp.utils.CVManager;
import java.awt.*;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.LineBorder;

/**
 * Production-ready Login UI with TOTP support and modern styling.
 * 
 * Developed by: azihad
 * Contact: azihad783@gmail.com
 * GitHub: AZtheE1
 */
public class LoginUI extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(LoginUI.class.getName());
    private final UserService userService = new UserService();

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;

    public LoginUI() {
        initUI();
    }

    private void initUI() {
        setTitle(Constants.app.name + " - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 550);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(UITheme.BACKGROUND_COLOR);
        mainPanel.setBorder(UITheme.MAIN_PADDING);

        // Header
        JLabel titleLabel = new JLabel("Welcome Back");
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(UITheme.PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Fields
        usernameField = createTextField("Username");
        passwordField = createPasswordField("Password");

        // Progress Bar
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setMaximumSize(new Dimension(350, 10));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Buttons
        loginButton = new JButton("Sign In");
        loginButton.setFont(UITheme.HEADER_FONT);
        loginButton.setBackground(UITheme.PRIMARY_COLOR);
        loginButton.setForeground(Color.WHITE);
        loginButton.setMaximumSize(new Dimension(350, 50));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> handleLogin());

        JButton registerButton = new JButton("Create New Account");
        registerButton.setFont(UITheme.REGULAR_FONT);
        registerButton.setForeground(UITheme.SECONDARY_COLOR);
        registerButton.setBorderPainted(false);
        registerButton.setContentAreaFilled(false);
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerButton.addActionListener(e -> {
            new RegistrationUI();
            dispose();
        });

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.SMALL_FONT);
        statusLabel.setForeground(UITheme.ERROR_COLOR);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(30));
        mainPanel.add(new JLabel("Username"));
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(usernameField);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(new JLabel("Password"));
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(passwordField);
        mainPanel.add(Box.createVerticalStrut(25));
        mainPanel.add(progressBar);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(loginButton);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(registerButton);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(statusLabel);
        mainPanel.add(Box.createVerticalGlue());

        add(mainPanel);
        setVisible(true);
    }

    private JTextField createTextField(String hint) {
        JTextField field = new JTextField();
        field.setMaximumSize(new Dimension(350, 40));
        field.setFont(UITheme.REGULAR_FONT);
        field.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
        return field;
    }

    private JPasswordField createPasswordField(String hint) {
        JPasswordField field = new JPasswordField();
        field.setMaximumSize(new Dimension(350, 40));
        field.setFont(UITheme.REGULAR_FONT);
        field.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
        return field;
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (!validateInput(username, password)) return;

        setLoading(true);
        SwingWorker<Optional<User>, Void> worker = new SwingWorker<>() {
            @Override
            protected Optional<User> doInBackground() {
                return userService.authenticate(username, password);
            }

            @Override
            protected void done() {
                try {
                    Optional<User> userOpt = get();
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        String code = JOptionPane.showInputDialog(LoginUI.this, 
                            "Enter the 6-digit code from your Authenticator app:", 
                            "Two-Factor Authentication", 
                            JOptionPane.QUESTION_MESSAGE);
                        
                        if (userService.validateTOTP(user.totpSecret(), code)) {
                            CVManager.logAction(user.id(), "LOGIN_SUCCESS", "User logged in with TOTP");
                            navigateToDashboard(user);
                        } else {
                            statusLabel.setText("Invalid TOTP code.");
                            CVManager.logAction(user.id(), "LOGIN_FAILURE", "Invalid TOTP code provided");
                        }
                    } else {
                        statusLabel.setText("Invalid username or password.");
                    }
                } catch (Exception e) {
                    LOGGER.severe("Login process failed: " + e.getMessage());
                    statusLabel.setText(Constants.MESSAGE_ERROR_UNEXPECTED);
                } finally {
                    setLoading(false);
                }
            }
        };
        worker.execute();
    }

    private boolean validateInput(String username, String password) {
        if (username.isEmpty()) {
            usernameField.setBorder(new LineBorder(UITheme.ERROR_COLOR, 1));
            statusLabel.setText("Username is required.");
            return false;
        }
        if (password.isEmpty()) {
            passwordField.setBorder(new LineBorder(UITheme.ERROR_COLOR, 1));
            statusLabel.setText("Password is required.");
            return false;
        }
        usernameField.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
        passwordField.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
        return true;
    }

    private void setLoading(boolean loading) {
        progressBar.setVisible(loading);
        loginButton.setEnabled(!loading);
        usernameField.setEnabled(!loading);
        passwordField.setEnabled(!loading);
    }

    private void navigateToDashboard(User user) {
        SwingUtilities.invokeLater(() -> {
            if (user.isAdmin()) {
                new AdminDashboard();
            } else {
                new UserDashboard(user);
            }
            dispose();
        });
    }
}