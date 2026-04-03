package com.cvreviewapp;

import com.cvreviewapp.services.UserService;
import com.cvreviewapp.utils.Constants;
import com.cvreviewapp.utils.UITheme;
import java.awt.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.LineBorder;

/**
 * Production-ready Registration UI with input validation and TOTP setup.
 * 
 * Developed by: azihad
 * Contact: azihad783@gmail.com
 * GitHub: AZtheE1
 */
public class RegistrationUI extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(RegistrationUI.class.getName());
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    
    private final UserService userService = new UserService();

    private JTextField usernameField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JPasswordField passwordField;
    private JTextField emailField;
    private JComboBox<String> roleBox;
    private JButton registerButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;

    public RegistrationUI() {
        initUI();
    }

    private void initUI() {
        setTitle(Constants.app.name + " - Registration");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 750); // Increased height for new fields
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(UITheme.BACKGROUND_COLOR);
        mainPanel.setBorder(UITheme.MAIN_PADDING);

        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(UITheme.PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        usernameField = createTextField();
        firstNameField = createTextField();
        lastNameField = createTextField();
        passwordField = createPasswordField();
        emailField = createTextField();
        roleBox = new JComboBox<>(new String[]{Constants.ROLE_USER, Constants.ROLE_ADMIN});
        roleBox.setMaximumSize(new Dimension(400, 40));

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setMaximumSize(new Dimension(400, 10));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        registerButton = new JButton("Register");
        registerButton.setFont(UITheme.HEADER_FONT);
        registerButton.setBackground(UITheme.SUCCESS_COLOR);
        registerButton.setForeground(Color.WHITE);
        registerButton.setMaximumSize(new Dimension(400, 50));
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerButton.addActionListener(e -> handleRegister());

        JButton backButton = new JButton("Back to Login");
        backButton.setFont(UITheme.REGULAR_FONT);
        backButton.setForeground(UITheme.SECONDARY_COLOR);
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(e -> {
            new LoginUI();
            dispose();
        });

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.SMALL_FONT);
        statusLabel.setForeground(UITheme.ERROR_COLOR);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(30));
        
        addFormField(mainPanel, "Username", usernameField);
        addFormField(mainPanel, "First Name", firstNameField);
        addFormField(mainPanel, "Last Name", lastNameField);
        addFormField(mainPanel, "Email", emailField);
        addFormField(mainPanel, "Password", passwordField);
        addFormField(mainPanel, "Role", roleBox);

        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(progressBar);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(registerButton);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(backButton);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(statusLabel);
        mainPanel.add(Box.createVerticalGlue());

        add(mainPanel);
        setVisible(true);
    }

    private void addFormField(JPanel panel, String label, JComponent field) {
        JLabel l = new JLabel(label);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(l);
        panel.add(Box.createVerticalStrut(5));
        panel.add(field);
        panel.add(Box.createVerticalStrut(15));
    }

    private JTextField createTextField() {
        JTextField f = new JTextField();
        f.setMaximumSize(new Dimension(400, 40));
        f.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
        return f;
    }

    private JPasswordField createPasswordField() {
        JPasswordField f = new JPasswordField();
        f.setMaximumSize(new Dimension(400, 40));
        f.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
        return f;
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = (String) roleBox.getSelectedItem();

        if (!validateInput(username, email, password, firstName, lastName)) return;

        setLoading(true);
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                if (userService.exists(username)) {
                    SwingUtilities.invokeLater(() -> statusLabel.setText("Username already exists."));
                    return false;
                }
                return userService.register(username, password, email, role, firstName, lastName);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(RegistrationUI.this, 
                            "Registration successful! Please log in.\nNote: TOTP protection is now active.", 
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        new LoginUI();
                        dispose();
                    }
                } catch (Exception e) {
                    LOGGER.severe("Registration failed: " + e.getMessage());
                    statusLabel.setText(Constants.MESSAGE_ERROR_UNEXPECTED);
                } finally {
                    setLoading(false);
                }
            }
        };
        worker.execute();
    }

    private boolean validateInput(String user, String email, String pass, String first, String last) {
        boolean valid = true;
        
        if (user.length() < 3) { usernameField.setBorder(new LineBorder(UITheme.ERROR_COLOR, 1)); valid = false; }
        else usernameField.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));

        if (first.isEmpty()) { firstNameField.setBorder(new LineBorder(UITheme.ERROR_COLOR, 1)); valid = false; }
        else firstNameField.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));

        if (last.isEmpty()) { lastNameField.setBorder(new LineBorder(UITheme.ERROR_COLOR, 1)); valid = false; }
        else lastNameField.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));

        if (!EMAIL_PATTERN.matcher(email).matches()) { emailField.setBorder(new LineBorder(UITheme.ERROR_COLOR, 1)); valid = false; }
        else emailField.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));

        if (pass.length() < 6) { passwordField.setBorder(new LineBorder(UITheme.ERROR_COLOR, 1)); valid = false; }
        else passwordField.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));

        if (!valid) statusLabel.setText("Please fix the highlighted fields.");
        return valid;
    }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(RegistrationUI.this, 
                            "Registration successful! Please log in.\nNote: TOTP protection is now active.", 
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        new LoginUI();
                        dispose();
                    }
                } catch (Exception e) {
                    LOGGER.severe("Registration failed: " + e.getMessage());
                    statusLabel.setText(Constants.MESSAGE_ERROR_UNEXPECTED);
                } finally {
                    setLoading(false);
                }
            }
        };
        worker.execute();
    }

    private boolean validateInput(String user, String email, String pass) {
        boolean valid = true;
        if (user.length() < 3) {
            usernameField.setBorder(new LineBorder(UITheme.ERROR_COLOR, 1));
            valid = false;
        } else usernameField.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            emailField.setBorder(new LineBorder(UITheme.ERROR_COLOR, 1));
            valid = false;
        } else emailField.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));

        if (pass.length() < 6) {
            passwordField.setBorder(new LineBorder(UITheme.ERROR_COLOR, 1));
            valid = false;
        } else passwordField.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));

        if (!valid) statusLabel.setText("Please fix the highlighted fields.");
        return valid;
    }

    private void setLoading(boolean loading) {
        progressBar.setVisible(loading);
        registerButton.setEnabled(!loading);
    }
}