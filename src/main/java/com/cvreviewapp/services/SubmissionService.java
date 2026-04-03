package com.cvreviewapp.services;

import com.cvreviewapp.models.Submission;
import com.cvreviewapp.models.User;
import com.cvreviewapp.utils.DBConnection;
import com.cvreviewapp.utils.Constants;
import com.cvreviewapp.utils.CVManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for handling CV submissions, storage, and retrieval.
 * 
 * Developed by: azihad
 * Contact: azihad783@gmail.com
 * GitHub: AZtheE1
 */
public class SubmissionService {
    private static final Logger LOGGER = Logger.getLogger(SubmissionService.class.getName());
    private final Path uploadRoot;

    public SubmissionService() {
        this.uploadRoot = Paths.get("uploads").toAbsolutePath().normalize();
        try {
            if (!Files.exists(uploadRoot)) {
                Files.createDirectories(uploadRoot);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize upload directory: " + uploadRoot, e);
        }
    }

    /**
     * Stored CV file and save metadata to database.
     */
    public Optional<Submission> uploadCV(File file, User user, String title, String description, String jobTitle) {
        // Path Traversal Security: Ensure the destination is within the upload root
        String safeName = UUID.randomUUID().toString() + ".pdf";
        Path destPath = uploadRoot.resolve(safeName).normalize();
        if (!destPath.startsWith(uploadRoot)) {
            LOGGER.warning("Path traversal attempt detected with filename: " + safeName);
            return Optional.empty();
        }

        try {
            Files.copy(file.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
            int id = saveMetadata(user.id(), title, description, destPath.toString(), file.getName(), file.length(), jobTitle);
            if (id > 0) {
                 return getById(id);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to store uploaded file: " + file.getName(), e);
        }
        return Optional.empty();
    }

    private int saveMetadata(int userId, String title, String description, String filePath, String fileName, long fileSize, String jobTitle) {
        String sql = "INSERT INTO " + Constants.TABLE_SUBMISSIONS + 
                    " (user_id, title, description, file_path, file_name, file_size, file_type, status, job_title) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, title);
            pstmt.setString(3, description);
            pstmt.setString(4, filePath);
            pstmt.setString(5, fileName);
            pstmt.setLong(6, fileSize);
            pstmt.setString(7, "application/pdf");
            pstmt.setString(8, Constants.STATUS_PENDING);
            pstmt.setString(9, jobTitle);
            
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                 if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving CV metadata for user: " + userId, e);
        }
        return -1;
    }

    public List<Submission> getUserSubmissions(int userId) {
        String sql = "SELECT * FROM " + Constants.TABLE_SUBMISSIONS + " WHERE user_id = ? ORDER BY submission_date DESC";
        List<Submission> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             pstmt.setInt(1, userId);
             try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSubmission(rs));
                }
             }
        } catch (SQLException e) {
             LOGGER.log(Level.SEVERE, "Error fetching submissions for user: " + userId, e);
        }
        return list;
    }

    public Optional<Submission> getById(int id) {
        String sql = "SELECT * FROM " + Constants.TABLE_SUBMISSIONS + " WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             pstmt.setInt(1, id);
             try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToSubmission(rs));
             }
        } catch (SQLException e) {
             LOGGER.log(Level.SEVERE, "Error fetching submission by ID: " + id, e);
        }
        return Optional.empty();
    }

    private Submission mapResultSetToSubmission(ResultSet rs) throws SQLException {
        return new Submission(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("file_path"),
            rs.getString("file_name"),
            rs.getLong("file_size"),
            rs.getString("file_type"),
            rs.getString("status"),
            rs.getTimestamp("submission_date"),
            rs.getTimestamp("updated_at"),
            (Integer) rs.getObject("reviewer_id"),
            rs.getTimestamp("review_date"),
            rs.getString("notes"),
            rs.getString("job_title")
        );
    }
}
