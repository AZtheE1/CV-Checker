package com.cvreviewapp.services;

import com.cvreviewapp.models.User;
import com.cvreviewapp.utils.DBConnection;
import com.cvreviewapp.utils.Constants;
import de.taimos.totp.TOTP;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service handling user-related business logic: Authentication, Registration, and TOTP.
 * 
 * Developed by: azihad
 * Contact: azihad783@gmail.com
 * GitHub: AZtheE1
 */
public class UserService {
    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());

    /**
     * Authenticates a user by username and password.
     * @return Optional<User> if authenticated successfully
     */
    public Optional<User> authenticate(String username, String password) {
        String sql = "SELECT id, username, password_hash, role, email, totp_secret FROM " + Constants.TABLE_USERS + " WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("password_hash");
                    if (BCrypt.checkpw(password, hash)) {
                        return Optional.of(new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            hash,
                            rs.getString("role"),
                            rs.getString("email"),
                            rs.getString("totp_secret")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Authentication error for user: " + username, e);
        }
        return Optional.empty();
    }

    /**
     * Validates a TOTP code for a user.
     */
    public boolean validateTOTP(String secret, String code) {
        if (secret == null || secret.isBlank()) return true; // Support bypass if not set (or adapt to your policy)
        String hexSecret = Hex.encodeHexString(new Base32().decode(secret));
        return TOTP.getOTP(hexSecret).equals(code);
    }

    /**
     * Registers a new user.
     */
    public boolean register(String username, String password, String email, String role) {
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        // Generate a new TOTP secret for the user
        byte[] buffer = new byte[10];
        new java.util.Random().nextBytes(buffer);
        String totpSecret = new Base32().encodeAsString(buffer);

        String sql = "INSERT INTO " + Constants.TABLE_USERS + " (username, password_hash, email, role, totp_secret) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, hash);
            pstmt.setString(3, email);
            pstmt.setString(4, role != null ? role : Constants.ROLE_USER);
            pstmt.setString(5, totpSecret);
            
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                LOGGER.info("User registered successfully: " + username);
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Registration error for user: " + username, e);
        }
        return false;
    }

    /**
     * Checks if a username already exists.
     */
    public boolean exists(String username) {
        String sql = "SELECT 1 FROM " + Constants.TABLE_USERS + " WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking user existence: " + username, e);
        }
        return false;
    }
}
