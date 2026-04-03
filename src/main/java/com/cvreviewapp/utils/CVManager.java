package com.cvreviewapp.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility for CV analysis and criteria check.
 * 
 * Developed by: azihad
 * Contact: azihad783@gmail.com
 * GitHub: AZtheE1
 */
public class CVManager {
    private static final Logger LOGGER = Logger.getLogger(CVManager.class.getName());

    private CVManager() {}

    /**
     * Checks a CV file against the requirements for a given job title.
     * Requirements are fetched dynamically from the database.
     * 
     * @param filePath Path to the CV PDF file
     * @param jobTitle The job title to check against
     * @return A map with results: missingSkills, hasQualification, hasExperience
     */
    public static Map<String, Object> checkCV(String filePath, String jobTitle) {
        Map<String, Object> result = new HashMap<>();
        String text = "";
        try {
            text = PDFReader.extractText(new File(filePath));
            if (text == null || text.isBlank()) {
                result.put("error", "Empty PDF content or extraction failed.");
                return result;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not read CV PDF: " + filePath, e);
            result.put("error", "Could not read CV PDF: " + e.getMessage());
            return result;
        }

        // Fetch dynamic criteria from database
        Map<String, String> criteria = fetchCriteriaForJob(jobTitle);
        if (criteria.isEmpty()) {
            result.put("error", "No criteria found for job title: " + jobTitle);
            return result;
        }

        List<String> missingSkills = new ArrayList<>();
        String skillsString = criteria.getOrDefault("skills", "");
        for (String skill : skillsString.split(",")) {
            String s = skill.trim();
            if (!s.isEmpty() && !text.toLowerCase().contains(s.toLowerCase())) {
                missingSkills.add(s);
            }
        }

        boolean hasQualification = text.toLowerCase().contains(criteria.getOrDefault("qualification", "").toLowerCase());
        boolean hasExperience = text.toLowerCase().contains(criteria.getOrDefault("experience", "").toLowerCase());

        result.put("missingSkills", missingSkills);
        result.put("hasQualification", hasQualification);
        result.put("hasExperience", hasExperience);
        result.put("jobTitle", jobTitle);
        
        return result;
    }

    private static Map<String, String> fetchCriteriaForJob(String jobTitle) {
        Map<String, String> map = new HashMap<>();
        String sql = "SELECT category, requirements FROM job_requirements WHERE job_title = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, jobTitle);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("category"), rs.getString("requirements"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error fetching criteria for: " + jobTitle, e);
        }
        return map;
    }

    /**
     * Logs actions securely using PreparedStatement.
     */
    public static void logAction(Integer userId, String action, String details) {
        String sql = "INSERT INTO " + Constants.TABLE_LOGS + " (user_id, action, details) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if (userId != null) pstmt.setInt(1, userId); else pstmt.setNull(1, Types.INTEGER);
            pstmt.setString(2, action);
            pstmt.setString(3, details);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to log action: " + action, e);
        }
    }
}