package com.cvreviewapp.utils;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

public class DBConnectionTest {
    @Test
    void testGetConnection() {
        try (Connection conn = DBConnection.getConnection()) {
            assertNotNull(conn);
            assertFalse(conn.isClosed());
        } catch (Exception e) {
            // Skip test if database is not available (common in CI/CD or development)
            Assumptions.assumeTrue(false, "Database not available: " + e.getMessage());
        }
    }
} 