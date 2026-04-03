package com.cvreviewapp;

/**
 * CV-Handler Application
 * 
 * Developed by: azihad
 * Contact: azihad783@gmail.com
 * GitHub: AZtheE1
 */

import com.formdev.flatlaf.FlatLightLaf;
import com.cvreviewapp.utils.DatabaseInitializer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    
    public static void main(String[] args) {
        // Initialize logging
        System.setProperty("java.util.logging.SimpleFormatter.format", 
                          "[%1$tF %1$tT] [%4$-7s] %2$s: %5$s%6$s%n");
        
        LOGGER.info("Starting CV Management Application...");
        
        try {
            // Initialize database
            if (!DatabaseInitializer.isDatabaseInitialized()) {
                LOGGER.info("Database not initialized. Setting up database...");
                DatabaseInitializer.initializeDatabase();
            } else {
                LOGGER.info("Database already initialized.");
            }
            
            // Set up UI look and feel
            UIManager.setLookAndFeel(new FlatLightLaf());
            LOGGER.info("UI Look and Feel initialized successfully.");
            
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to initialize application", ex);
            System.err.println("Failed to initialize application: " + ex.getMessage());
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            LOGGER.info("Launching HomePage...");
            new HomePage();
        });
    }
} 