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
import java.sql.ResultSet;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.cvreviewapp.utils.BackgroundPanel;
import com.cvreviewapp.utils.DBConnection;
import com.cvreviewapp.utils.EmailUtil;
import com.cvreviewapp.utils.HashUtil;

public class LoginUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;
    private String last2FACode;

    private static final Logger logger = Logger.getLogger(LoginUI.class.getName());

    public LoginUI() {
        setTitle("CV Management App - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        card.setMaximumSize(new Dimension(520, 480));
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
                System.err.println("[LoginUI] Resource not found: /com/cvreviewapp/background.jpg");
            }
        } catch (Exception ex) {}
        logo.setBorder(new EmptyBorder(24, 0, 0, 0));

        JLabel title = new JLabel("CV Management Login");
        title.setFont(new Font("Arial", Font.BOLD, 32));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(24, 0, 24, 0));
        title.setForeground(new Color(30, 30, 30));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        loginButton.setToolTipText("Sign in to your account");
        registerButton.setToolTipText("Create a new account");
        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setForeground(Color.RED);

        loginButton.setFont(new Font("Arial", Font.BOLD, 20));
        registerButton.setFont(new Font("Arial", Font.BOLD, 20));
        loginButton.setMaximumSize(new Dimension(180, 50));
        registerButton.setMaximumSize(new Dimension(180, 50));
        loginButton.setPreferredSize(new Dimension(180, 50));
        registerButton.setPreferredSize(new Dimension(180, 50));
        loginButton.setFocusPainted(false);
        registerButton.setFocusPainted(false);
        loginButton.setBackground(new Color(0, 120, 215));
        loginButton.setForeground(Color.WHITE);
        registerButton.setBackground(new Color(0, 153, 51));
        registerButton.setForeground(Color.WHITE);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setOpaque(false);
        fieldsPanel.setLayout(new GridLayout(4, 2, 0, 12));
        fieldsPanel.setMaximumSize(new Dimension(400, 120));
        fieldsPanel.add(new JLabel("Username:"));
        fieldsPanel.add(usernameField);
        fieldsPanel.add(new JLabel("Password:"));
        fieldsPanel.add(passwordField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(loginButton);
        buttonPanel.add(Box.createHorizontalStrut(16));
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

        loginButton.setMnemonic('L');
        registerButton.setMnemonic('R');
        getRootPane().setDefaultButton(loginButton);
        usernameField.requestFocusInWindow();

        loginButton.addActionListener(e -> {
            handleLogin(e);
            if (statusLabel.getText().toLowerCase().contains("success")) {
                statusLabel.setForeground(new Color(0, 153, 51));
            } else {
                statusLabel.setForeground(Color.RED);
            }
        });
        registerButton.addActionListener(e -> {
            RegistrationUI reg = new RegistrationUI();
            reg.setExtendedState(JFrame.MAXIMIZED_BOTH);
            dispose();
        });

        setVisible(true);
    }

    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password.");
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String hash = rs.getString("password_hash");
                String role = rs.getString("role");
                String email = rs.getString("email");
                if (HashUtil.verifyPassword(password, hash)) {
                    last2FACode = EmailUtil.send2FACode(email);
                    if (last2FACode == null) {
                        statusLabel.setText("Failed to send 2FA code. Please check your email settings.");
                        log("User '" + username + "' failed 2FA (email send error).");
                        return;
                    }
                    String code = JOptionPane.showInputDialog(this, "Enter the 2FA code sent to your email:");
                    if (last2FACode.equals(code)) {
                        statusLabel.setText("Login successful!");
                        log("User '" + username + "' logged in as " + role);
                        dispose();
                        try {
                            if ("admin".equals(role)) {
                                new AdminDashboard();
                            } else {
                                UserDashboard userDashboard = new UserDashboard(username);
                                if (userDashboard == null) {
                                    throw new Exception("Failed to create UserDashboard instance");
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(null, "Failed to load dashboard: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            log("Dashboard error: " + ex.getMessage());
                        }
                    } else {
                        statusLabel.setText("Invalid 2FA code. Please try again.");
                        log("User '" + username + "' failed 2FA.");
                        // Do NOT dispose(); let the user try again
                    }
                } else {
                    statusLabel.setText("Invalid password.");
                    log("User '" + username + "' failed login (bad password).");
                }
            } else {
                statusLabel.setText("User not found.");
                log("Login attempt for non-existent user '" + username + "'.");
            }
        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
            log("Login error: " + ex.getMessage());
        }
    }

    private void log(String message) {
        logger.info(message);
    }
} 