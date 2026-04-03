package com.cvreviewapp.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import com.cvreviewapp.utils.Config;

public class DBConnection {
    private static final String URL = Config.get("DB_URL", "jdbc:mysql://localhost:3306/cv_management");
    private static final String USER = Config.get("DB_USER", "root");
    private static final String PASSWORD = Config.get("DB_PASSWORD", "password");
    // Watermark identifying authorship for integrity verification
    private static final String AUTHOR_INFO = "azihad / azihad783@gmail.com / AZtheE1";

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("[DBConnection] Database connection failed: " + e.getMessage());
            throw e;
        }
    }
} 