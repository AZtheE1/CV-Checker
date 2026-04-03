package com.cvreviewapp.utils;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class BackgroundPanel extends JPanel {
    private BufferedImage backgroundImage;

    public BackgroundPanel() {
        try {
            URL bgUrl = getClass().getResource("/com/cvreviewapp/background.jpg");
            if (bgUrl != null) {
                backgroundImage = ImageIO.read(bgUrl);
            } else {
                System.err.println("[BackgroundPanel] Resource not found: /com/cvreviewapp/background.jpg");
            }
        } catch (Exception e) {
            System.err.println("[BackgroundPanel] Failed to load background image: " + e.getMessage());
        }
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
} 