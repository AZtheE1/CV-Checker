package com.cvreviewapp.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseTest {
    private static final Logger LOGGER = Logger.getLogger(DatabaseTest.class.getName());
    
    public static void testDatabaseConnection() {
        LOGGER.info("Testing database connection...");
        
        try (Connection conn = DBConnection.getConnection()) {
            LOGGER.info("Database connection successful!");
            
            // Test basic query
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users")) {
                
                if (rs.next()) {
                    int userCount = rs.getInt("count");
                    LOGGER.info("Found " + userCount + " users in database");
                }
            }
            
            // Test review criteria
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM review_criteria")) {
                
                if (rs.next()) {
                    int criteriaCount = rs.getInt("count");
                    LOGGER.info("Found " + criteriaCount + " review criteria in database");
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection test failed", e);
            throw new RuntimeException("Database connection test failed", e);
        }
    }
    
    public static void testTableStructure() {
        LOGGER.info("Testing table structure...");
        
        try (Connection conn = DBConnection.getConnection()) {
            String[] tables = {"users", "cv_submissions", "review_criteria", "cv_reviews", "review_sessions", "user_sessions", "system_logs"};
            
            for (String table : tables) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("DESCRIBE " + table)) {
                    
                    LOGGER.info("Table '" + table + "' structure:");
                    while (rs.next()) {
                        String field = rs.getString("Field");
                        String type = rs.getString("Type");
                        String key = rs.getString("Key");
                        LOGGER.info("  - " + field + " (" + type + ") " + (key != null ? "[" + key + "]" : ""));
                    }
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Table structure test failed", e);
            throw new RuntimeException("Table structure test failed", e);
        }
    }
    
    public static void main(String[] args) {
        // Initialize logging
        System.setProperty("java.util.logging.SimpleFormatter.format", 
                          "[%1$tF %1$tT] [%4$-7s] %2$s: %5$s%6$s%n");
        
        try {
            // Initialize database if needed
            if (!DatabaseInitializer.isDatabaseInitialized()) {
                LOGGER.info("Database not initialized. Setting up database...");
                DatabaseInitializer.initializeDatabase();
            }
            
            // Test connection
            testDatabaseConnection();
            
            // Test table structure
            testTableStructure();
            
            LOGGER.info("All database tests completed successfully!");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Database test failed", e);
            System.err.println("Database test failed: " + e.getMessage());
        }
    }
} 