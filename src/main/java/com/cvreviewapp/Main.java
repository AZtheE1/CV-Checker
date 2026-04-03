package com.cvreviewapp;

import com.cvreviewapp.utils.DatabaseInitializer;
import com.cvreviewapp.utils.UITheme;
import com.formdev.flatlaf.FlatLightLaf;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * CV-Handler Application Entry Point.
 * 
 * Developed by: azihad
 * Contact: azihad783@gmail.com
 * GitHub: AZtheE1
 */
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    
    public static void main(String[] args) {
        // Professional log formatting
        System.setProperty("java.util.logging.SimpleFormatter.format", 
                          "[%1$tF %1$tT] [%4$-7s] %2$s: %5$s%6$s%n");
        
        LOGGER.info("Initializing CV-Handler Production System...");
        
        try {
            // 1. Initialize DB Layout (if first run)
            DatabaseInitializer.initializeDatabase();
            
            // 2. Set Theme (FlatLaf)
            UIManager.setLookAndFeel(new FlatLightLaf());
            UITheme.setupGlobalStyles(); // Apply extra production styling
            
            LOGGER.info("System subsystems initialized successfully.");
            
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "FATAL: Application startup failure", ex);
            System.err.println("The application could not start. Check logs/application.log for details.");
            return;
        }
        
        // 3. Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                new HomePage();
                LOGGER.info("HomePage launched successfully.");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to launch main window", e);
            }
        });
    }
}