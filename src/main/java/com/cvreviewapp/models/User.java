package com.cvreviewapp.models;

/**
 * User data container using Java 21 Records.
 * 
 * Developed by: azihad
 * Contact: azihad783@gmail.com
 * GitHub: AZtheE1
 */
public record User(
    int id,
    String username,
    String passwordHash,
    String role,
    String email,
    String totpSecret
) {
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }
}