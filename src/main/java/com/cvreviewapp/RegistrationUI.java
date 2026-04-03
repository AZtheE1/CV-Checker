package com.cvreviewapp;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.cvreviewapp.utils.BackgroundPanel;
import com.cvreviewapp.utils.DBConnection;
import com.cvreviewapp.utils.HashUtil;

public class RegistrationUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;
    private JButton registerButton;
    private JLabel statusLabel;
    private JTextField emailField;
    private JTextField firstNameField;
    private JTextField lastNameField;

    private static final Logger logger = Logger.getLogger(RegistrationUI.class.getName());
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", Pattern.CASE_INSENSITIVE);
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,32}$");

    public RegistrationUI() {
        setTitle("User Registration");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        BackgroundPanel bgPanel = new BackgroundPanel();
        bgPanel.setLayout(new GridBagLayout());

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0,0,0,30));
                g2.fillRoundRect(8, 8, getWidth()-16, getHeight()-16, 32, 32);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new LineBorder(new Color(180,180,180,180), 2, true));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(255, 255, 255, 220));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setAlignmentY(Component.CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(520, 540));
        card.setMinimumSize(new Dimension(420, 340));

        // Logo
        JLabel logo = new JLabel();
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        try {
            java.net.URL bgUrl = getClass().getResource("/com/cvreviewapp/background.jpg");
            if (bgUrl != null) {
                ImageIcon icon = new ImageIcon(bgUrl);
                Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                logo.setIcon(new ImageIcon(img));
            } else {
                System.err.println("[RegistrationUI] Resource not found: /com/cvreviewapp/background.jpg");
            }
        } catch (Exception ex) {}
        logo.setBorder(new EmptyBorder(24, 0, 0, 0));

        JLabel title = new JLabel("Register New User");
        title.setFont(new Font("Arial", Font.BOLD, 32));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(24, 0, 24, 0));
        title.setForeground(new Color(30, 30, 30));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        roleBox = new JComboBox<>(new String[]{"user", "admin"});
        registerButton = new JButton("Register");
        registerButton.setToolTipText("Create a new user account");
        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setForeground(Color.RED);
        emailField = new JTextField(20);
        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);

        registerButton.setFont(new Font("Arial", Font.BOLD, 20));
        registerButton.setMaximumSize(new Dimension(180, 50));
        registerButton.setPreferredSize(new Dimension(180, 50));
        registerButton.setFocusPainted(false);
        registerButton.setBackground(new Color(0, 153, 51));
        registerButton.setForeground(Color.WHITE);
        registerButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setOpaque(false);
        fieldsPanel.setLayout(new GridLayout(8, 2, 0, 12));
        fieldsPanel.add(new JLabel("First Name:"));
        fieldsPanel.add(firstNameField);
        fieldsPanel.add(new JLabel("Last Name:"));
        fieldsPanel.add(lastNameField);
        fieldsPanel.add(new JLabel("Username:"));
        fieldsPanel.add(usernameField);
        fieldsPanel.add(new JLabel("Password:"));
        fieldsPanel.add(passwordField);
        fieldsPanel.add(new JLabel("Role:"));
        fieldsPanel.add(roleBox);
        fieldsPanel.add(new JLabel("Email:"));
        fieldsPanel.add(emailField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(registerButton);

        // Add Back button
        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Arial", Font.PLAIN, 16));
        backButton.setBackground(new Color(220, 220, 220));
        backButton.setFocusPainted(false);
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> {
            new HomePage();
            dispose();
        });

        card.add(logo);
        card.add(title);
        card.add(Box.createVerticalStrut(10));
        card.add(backButton);
        card.add(Box.createVerticalStrut(10));
        card.add(fieldsPanel);
        card.add(Box.createVerticalStrut(20));
        card.add(buttonPanel);
        card.add(Box.createVerticalStrut(16));
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(16));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        bgPanel.add(card, gbc);

        setContentPane(bgPanel);

        registerButton.setMnemonic('R');
        getRootPane().setDefaultButton(registerButton);
        usernameField.requestFocusInWindow();

        registerButton.addActionListener(e -> {
            handleRegister(e);
            if (statusLabel.getText().toLowerCase().contains("success")) {
                statusLabel.setForeground(new Color(0, 153, 51));
            } else {
                statusLabel.setForeground(Color.RED);
            }
        });

        setVisible(true);
    }

    private void handleRegister(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = (String) roleBox.getSelectedItem();
        String email = emailField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            statusLabel.setText("Please enter all fields.");
            return;
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            statusLabel.setText("Username must be 3-32 characters, letters, numbers, or _ only.");
            return;
        }
        if (password.length() < 6) {
            statusLabel.setText("Password must be at least 6 characters.");
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            statusLabel.setText("Please enter a valid email address.");
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO users (username, password_hash, role, email, first_name, last_name) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, HashUtil.hashPassword(password));
            stmt.setString(3, role);
            stmt.setString(4, email);
            stmt.setString(5, firstName);
            stmt.setString(6, lastName);
            stmt.executeUpdate();
            statusLabel.setText("Registration successful!");
            log("Registered new user: " + username + " (" + role + ")");
            dispose();
            new HomePage();
        } catch (Exception ex) {
            if (ex.getMessage().contains("Duplicate")) {
                statusLabel.setText("Username already exists.");
            } else {
                statusLabel.setText("Error: " + ex.getMessage());
            }
            log("Registration error: " + ex.getMessage());
        }
    }

    private void log(String message) {
        logger.info(message);
    }
} 