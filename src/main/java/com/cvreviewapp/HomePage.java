package com.cvreviewapp;

import com.cvreviewapp.utils.Constants;
import com.cvreviewapp.utils.UITheme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.LineBorder;

/**
 * Production-ready Home Page with professional branding and intuitive navigation.
 * 
 * Developed by: azihad
 * Contact: azihad783@gmail.com
 * GitHub: AZtheE1
 */
public class HomePage extends JFrame {

    public HomePage() {
        initUI();
    }

    private void initUI() {
        setTitle(Constants.app.name + " - Welcome");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(UITheme.PRIMARY_COLOR);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(500, 450));
        card.setMaximumSize(new Dimension(500, 450));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0, 0, 0, 50), 1),
            BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));

        JLabel logoLabel = new JLabel(Constants.app.name);
        logoLabel.setFont(UITheme.TITLE_FONT);
        logoLabel.setForeground(UITheme.PRIMARY_COLOR);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sloganLabel = new JLabel("Streamlined Recruitment & CV Analysis");
        sloganLabel.setFont(UITheme.REGULAR_FONT);
        sloganLabel.setForeground(Color.GRAY);
        sloganLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginBtn = createPrimaryButton("Access My Account");
        loginBtn.addActionListener(e -> {
            new LoginUI();
            dispose();
        });

        JButton registerBtn = createSecondaryButton("Register New User");
        registerBtn.addActionListener(e -> {
            new RegistrationUI();
            dispose();
        });

        card.add(Box.createVerticalGlue());
        card.add(logoLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(sloganLabel);
        card.add(Box.createVerticalGlue());
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(20));
        card.add(registerBtn);
        card.add(Box.createVerticalGlue());

        mainPanel.add(card);
        add(mainPanel);
        setVisible(true);
    }

    private JButton createPrimaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(UITheme.HEADER_FONT);
        b.setBackground(UITheme.PRIMARY_COLOR);
        b.setForeground(Color.WHITE);
        b.setMaximumSize(new Dimension(400, 55));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton createSecondaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(UITheme.HEADER_FONT);
        b.setBackground(Color.WHITE);
        b.setForeground(UITheme.PRIMARY_COLOR);
        b.setBorder(new LineBorder(UITheme.PRIMARY_COLOR, 2));
        b.setMaximumSize(new Dimension(400, 55));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HomePage::new);
    }
}