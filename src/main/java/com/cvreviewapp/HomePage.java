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
import java.awt.Image;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.cvreviewapp.utils.BackgroundPanel;

public class HomePage extends JFrame {
    public HomePage() {
        setTitle("Welcome to CV Management App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(false);
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
        card.setMaximumSize(new Dimension(520, 420));
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
                System.err.println("[HomePage] Resource not found: /com/cvreviewapp/background.jpg");
            }
        } catch (Exception ex) {}
        logo.setBorder(new EmptyBorder(24, 0, 0, 0));

        JLabel title = new JLabel("Welcome to CV Management App");
        title.setFont(new Font("Arial", Font.BOLD, 36));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(24, 0, 24, 0));
        title.setForeground(new Color(30, 30, 30));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        loginButton.setFont(new Font("Arial", Font.BOLD, 22));
        registerButton.setFont(new Font("Arial", Font.BOLD, 22));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(220, 60));
        registerButton.setMaximumSize(new Dimension(220, 60));
        loginButton.setPreferredSize(new Dimension(220, 60));
        registerButton.setPreferredSize(new Dimension(220, 60));
        loginButton.setFocusPainted(false);
        registerButton.setFocusPainted(false);
        loginButton.setBackground(new Color(0, 120, 215));
        loginButton.setForeground(Color.WHITE);
        registerButton.setBackground(new Color(0, 153, 51));
        registerButton.setForeground(Color.WHITE);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setToolTipText("Sign in to your account");
        registerButton.setToolTipText("Create a new account");

        card.add(logo);
        card.add(title);
        card.add(Box.createVerticalStrut(40));
        card.add(loginButton);
        card.add(Box.createVerticalStrut(20));
        card.add(registerButton);
        card.add(Box.createVerticalStrut(40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        bgPanel.add(card, gbc);

        setContentPane(bgPanel);

        loginButton.addActionListener(e -> {
            LoginUI login = new LoginUI();
            login.setExtendedState(JFrame.MAXIMIZED_BOTH);
            dispose();
        });
        registerButton.addActionListener(e -> {
            RegistrationUI reg = new RegistrationUI();
            reg.setExtendedState(JFrame.MAXIMIZED_BOTH);
            dispose();
        });

        setVisible(true);
    }
} 