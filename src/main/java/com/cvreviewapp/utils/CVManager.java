package com.cvreviewapp.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cvreviewapp.models.Submission;
import com.cvreviewapp.models.User;

public class CVManager {
    private static final Logger LOGGER = Logger.getLogger(CVManager.class.getName());
    private static final String UPLOAD_DIR = "uploads";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    static {
        // Ensure upload directory exists
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Uploads a CV file for a user, validates PDF, stores file, and saves metadata.
     * @param file The PDF file to upload
     * @param user The user uploading the file
     * @param title Title for the CV
     * @param description Description for the CV
     * @param jobTitle The job title associated with the CV submission
     * @return Submission object if successful, null otherwise
     */
    public static Submission uploadCV(File file, User user, String title, String description, String jobTitle) {
        // Validate file
        if (file == null || !file.exists()) {
            LOGGER.warning("File does not exist.");
            return null;
        }
        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            LOGGER.warning("File is not a PDF.");
            return null;
        }
        if (file.length() > MAX_FILE_SIZE) {
            LOGGER.warning("File exceeds maximum allowed size.");
            return null;
        }
        // Optionally: check PDF magic number
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] header = new byte[4];
            if (fis.read(header) != 4 || !(header[0] == 0x25 && header[1] == 0x50 && header[2] == 0x44 && header[3] == 0x46)) {
                LOGGER.warning("File is not a valid PDF (magic number check failed).");
                return null;
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to read file for validation", e);
            return null;
        }
        // Sanitize file name
        String safeFileName = UUID.randomUUID().toString() + ".pdf";
        File destFile = new File(UPLOAD_DIR, safeFileName);
        try {
            Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to store uploaded file", e);
            return null;
        }
        // Save metadata to database
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO cv_submissions (user_id, title, description, file_path, file_name, file_size, file_type, status, job_title) VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING', ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, user.getId());
                pstmt.setString(2, title);
                pstmt.setString(3, description);
                pstmt.setString(4, destFile.getAbsolutePath());
                pstmt.setString(5, file.getName());
                pstmt.setLong(6, file.length());
                pstmt.setString(7, "application/pdf");
                pstmt.setString(8, jobTitle);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    LOGGER.warning("Failed to insert CV metadata into database.");
                    return null;
                }
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int submissionId = generatedKeys.getInt(1);
                        Submission submission = new Submission(
                            submissionId,
                            user.getId(),
                            title,
                            description,
                            destFile.getAbsolutePath(),
                            file.getName(),
                            file.length(),
                            "application/pdf",
                            "PENDING",
                            new java.sql.Timestamp(System.currentTimeMillis()),
                            null, // updatedAt
                            null, // reviewerId
                            null, // reviewDate
                            null,  // notes
                            jobTitle
                        );
                        LOGGER.info("CV uploaded and metadata saved. Submission ID: " + submissionId);
                        return submission;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during CV upload", e);
        }
        return null;
    }

    /**
     * Returns a list of all CV submissions for a given user.
     */
    public static List<Submission> getUserCVs(int userId) {
        List<Submission> submissions = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id, user_id, title, description, file_path, file_name, file_size, file_type, status, submission_date, updated_at, reviewer_id, review_date, notes, job_title FROM cv_submissions WHERE user_id = ? ORDER BY submission_date DESC";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        submissions.add(new Submission(
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
                            (Integer)rs.getObject("reviewer_id"),
                            rs.getTimestamp("review_date"),
                            rs.getString("notes"),
                            rs.getString("job_title")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while fetching user CVs", e);
        }
        return submissions;
    }

    /**
     * Retrieves the CV file for a given submission ID.
     */
    public static File getCVFile(int submissionId) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT file_path FROM cv_submissions WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, submissionId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String filePath = rs.getString("file_path");
                        File file = new File(filePath);
                        if (file.exists()) {
                            return file;
                        } else {
                            LOGGER.warning("File not found on disk: " + filePath);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while retrieving CV file", e);
        }
        return null;
    }

    /**
     * Returns the CV file as a byte array for a given submission ID.
     */
    public static byte[] getCVFileAsBytes(int submissionId) {
        File file = getCVFile(submissionId);
        if (file != null && file.exists()) {
            try {
                return Files.readAllBytes(file.toPath());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to read CV file as bytes", e);
            }
        }
        return null;
    }

    /**
     * Deletes a CV submission by ID. Removes file from disk and record from database.
     * Returns true if successful.
     */
    public static boolean deleteCV(int submissionId, int userId) {
        try (Connection conn = DBConnection.getConnection()) {
            // Get file path first
            String selectSql = "SELECT file_path FROM cv_submissions WHERE id = ? AND user_id = ?";
            String filePath = null;
            try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
                pstmt.setInt(1, submissionId);
                pstmt.setInt(2, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        filePath = rs.getString("file_path");
                    } else {
                        LOGGER.warning("No CV found for deletion with id=" + submissionId + ", userId=" + userId);
                        return false;
                    }
                }
            }
            // Delete DB record
            String deleteSql = "DELETE FROM cv_submissions WHERE id = ? AND user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                pstmt.setInt(1, submissionId);
                pstmt.setInt(2, userId);
                int affected = pstmt.executeUpdate();
                if (affected == 0) {
                    LOGGER.warning("Failed to delete CV record from database.");
                    return false;
                }
            }
            // Delete file from disk
            if (filePath != null) {
                File file = new File(filePath);
                if (file.exists() && !file.delete()) {
                    LOGGER.warning("Failed to delete file from disk: " + filePath);
                }
            }
            LOGGER.info("CV deleted successfully. Submission ID: " + submissionId);
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during CV deletion", e);
        }
        return false;
    }

    /**
     * Updates the title and description of a CV submission.
     * Returns true if successful.
     */
    public static boolean updateCVMetadata(int submissionId, int userId, String newTitle, String newDescription) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE cv_submissions SET title = ?, description = ? WHERE id = ? AND user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newTitle);
                pstmt.setString(2, newDescription);
                pstmt.setInt(3, submissionId);
                pstmt.setInt(4, userId);
                int affected = pstmt.executeUpdate();
                if (affected > 0) {
                    LOGGER.info("CV metadata updated. Submission ID: " + submissionId);
                    return true;
                } else {
                    LOGGER.warning("No CV found for update with id=" + submissionId + ", userId=" + userId);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during CV metadata update", e);
        }
        return false;
    }

    /**
     * Lists all CV submissions, with optional status/user filter and pagination.
     */
    public static List<Submission> getAllCVs(String status, Integer userId, int limit, int offset) {
        List<Submission> submissions = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id, user_id, title, description, file_path, file_name, file_size, file_type, status, submission_date, updated_at, reviewer_id, review_date, notes, job_title FROM cv_submissions WHERE 1=1");
        if (status != null) sql.append(" AND status = ?");
        if (userId != null) sql.append(" AND user_id = ?");
        sql.append(" ORDER BY submission_date DESC LIMIT ? OFFSET ?");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (status != null) pstmt.setString(idx++, status);
            if (userId != null) pstmt.setInt(idx++, userId);
            pstmt.setInt(idx++, limit);
            pstmt.setInt(idx, offset);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    submissions.add(new Submission(
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
                        (Integer)rs.getObject("reviewer_id"),
                        rs.getTimestamp("review_date"),
                        rs.getString("notes"),
                        rs.getString("job_title")
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while fetching all CVs", e);
        }
        return submissions;
    }

    /**
     * Advanced search/filter for CV submissions.
     * All parameters are optional (null = no filter).
     */
    public static List<Submission> searchCVs(
            String status,
            Integer userId,
            String keyword,
            Timestamp fromDate,
            Timestamp toDate,
            int limit,
            int offset
    ) {
        List<Submission> submissions = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id, user_id, title, description, file_path, file_name, file_size, file_type, status, submission_date, updated_at, reviewer_id, review_date, notes, job_title FROM cv_submissions WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        if (userId != null) {
            sql.append(" AND user_id = ?");
            params.add(userId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (title LIKE ? OR description LIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        if (fromDate != null) {
            sql.append(" AND submission_date >= ?");
            params.add(fromDate);
        }
        if (toDate != null) {
            sql.append(" AND submission_date <= ?");
            params.add(toDate);
        }
        sql.append(" ORDER BY submission_date DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            for (Object param : params) {
                if (param instanceof String) pstmt.setString(idx++, (String) param);
                else if (param instanceof Integer) pstmt.setInt(idx++, (Integer) param);
                else if (param instanceof Timestamp) pstmt.setTimestamp(idx++, (Timestamp) param);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    submissions.add(new Submission(
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
                        (Integer)rs.getObject("reviewer_id"),
                        rs.getTimestamp("review_date"),
                        rs.getString("notes"),
                        rs.getString("job_title")
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during CV search/filter", e);
        }
        return submissions;
    }

    /**
     * Updates the status of a CV submission. Optionally sets reviewer and notes.
     */
    public static boolean updateCVStatus(int submissionId, String newStatus, Integer reviewerId, String notes) {
        String sql = "UPDATE cv_submissions SET status = ?, reviewer_id = ?, review_date = CURRENT_TIMESTAMP, notes = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            if (reviewerId != null) pstmt.setInt(2, reviewerId); else pstmt.setNull(2, java.sql.Types.INTEGER);
            pstmt.setString(3, notes);
            pstmt.setInt(4, submissionId);
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                LOGGER.info("CV status updated. Submission ID: " + submissionId + ", new status: " + newStatus);
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during CV status update", e);
        }
        return false;
    }

    /**
     * Adds or updates a review score/comment for a submission/criteria.
     */
    public static boolean addReviewScore(int submissionId, int reviewerId, int criteriaId, double score, String comments) {
        String sql = "INSERT INTO cv_reviews (cv_submission_id, reviewer_id, criteria_id, score, comments) VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE score = VALUES(score), comments = VALUES(comments), review_date = CURRENT_TIMESTAMP";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, submissionId);
            pstmt.setInt(2, reviewerId);
            pstmt.setInt(3, criteriaId);
            pstmt.setDouble(4, score);
            pstmt.setString(5, comments);
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                LOGGER.info("Review score added/updated. Submission ID: " + submissionId + ", Reviewer: " + reviewerId + ", Criteria: " + criteriaId);
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during review score add/update", e);
        }
        return false;
    }

    /**
     * Logs an action to the system_logs table.
     */
    public static void logAction(Integer userId, String action, String details, String ipAddress, String userAgent, String logLevel) {
        String sql = "INSERT INTO system_logs (user_id, action, details, ip_address, user_agent, log_level) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (userId != null) pstmt.setInt(1, userId); else pstmt.setNull(1, java.sql.Types.INTEGER);
            pstmt.setString(2, action);
            pstmt.setString(3, details);
            pstmt.setString(4, ipAddress);
            pstmt.setString(5, userAgent);
            pstmt.setString(6, logLevel != null ? logLevel : "INFO");
            pstmt.executeUpdate();
            LOGGER.fine("Action logged: " + action);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to log action to system_logs", e);
        }
    }

    /**
     * Fetches a User object by username.
     */
    public static com.cvreviewapp.models.User getUserByUsername(String username) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id, username, password_hash, role, email FROM users WHERE username = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return new com.cvreviewapp.models.User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("role"),
                            rs.getString("email")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while fetching user by username", e);
        }
        return null;
    }

    // Predefined job requirements data
    public static class JobRequirement {
        public String jobTitle;
        public java.util.List<String> skills;
        public String qualification;
        public String experience;
        public JobRequirement(String jobTitle, java.util.List<String> skills, String qualification, String experience) {
            this.jobTitle = jobTitle;
            this.skills = skills;
            this.qualification = qualification;
            this.experience = experience;
        }
    }

    // Static list of job requirements (can be moved to DB later)
    public static final java.util.List<JobRequirement> JOB_REQUIREMENTS = java.util.Arrays.asList(
        new JobRequirement("Java Developer", java.util.Arrays.asList("Java", "Spring Boot", "Hibernate", "REST APIs", "OOP", "Maven"), "Bachelor's Degree in Computer Science", "2+ years"),
        new JobRequirement("Frontend Developer", java.util.Arrays.asList("HTML", "CSS", "JavaScript", "React", "Responsive Design"), "Bachelor's in IT or related field", "1+ year"),
        new JobRequirement("Backend Developer", java.util.Arrays.asList("Node.js", "Express", "MongoDB", "REST APIs", "JWT"), "Bachelor's in Computer Science or equivalent", "2+ years"),
        new JobRequirement("Full Stack Developer", java.util.Arrays.asList("HTML", "CSS", "JavaScript", "React", "Node.js", "MongoDB"), "BSc in Computer Science or related field", "3+ years"),
        new JobRequirement("Data Analyst", java.util.Arrays.asList("SQL", "Python", "Excel", "Tableau", "Data Visualization", "Statistics"), "Bachelor's in Data Science, Statistics, or related", "1-3 years"),
        new JobRequirement("Data Scientist", java.util.Arrays.asList("Python", "Machine Learning", "Pandas", "TensorFlow", "Data Wrangling"), "Master's in Data Science or related", "3+ years"),
        new JobRequirement("Android Developer", java.util.Arrays.asList("Java", "Kotlin", "Android Studio", "XML", "Firebase"), "BSc in Computer Science or Software Engineering", "1+ year"),
        new JobRequirement("iOS Developer", java.util.Arrays.asList("Swift", "Objective-C", "Xcode", "UI/UX Design", "CoreData"), "Bachelor's in Software Engineering", "2+ years"),
        new JobRequirement("Machine Learning Engineer", java.util.Arrays.asList("Python", "scikit-learn", "TensorFlow", "Pandas", "ML Algorithms"), "Master's or Bachelor's in AI, ML, or CS", "2+ years"),
        new JobRequirement("UI/UX Designer", java.util.Arrays.asList("Figma", "Adobe XD", "Wireframing", "User Research", "Prototyping"), "Bachelor’s in Design or equivalent", "1+ year"),
        new JobRequirement("Graphic Designer", java.util.Arrays.asList("Photoshop", "Illustrator", "InDesign", "Creativity", "Typography"), "Bachelor’s in Graphic Design or Fine Arts", "1+ year"),
        new JobRequirement("DevOps Engineer", java.util.Arrays.asList("CI/CD", "Docker", "Kubernetes", "AWS", "Jenkins", "Linux"), "Bachelor's in Computer Science or equivalent", "2+ years"),
        new JobRequirement("Cybersecurity Analyst", java.util.Arrays.asList("Network Security", "Firewalls", "Penetration Testing", "SIEM", "Encryption"), "Bachelor’s in Cybersecurity or IT Security", "2+ years"),
        new JobRequirement("Cloud Engineer", java.util.Arrays.asList("AWS", "Azure", "CloudFormation", "Docker", "Terraform"), "Bachelor’s in IT or Cloud Computing", "3+ years"),
        new JobRequirement("Database Administrator", java.util.Arrays.asList("MySQL", "PostgreSQL", "Oracle", "Backup", "Performance Tuning"), "Bachelor’s in Information Systems or related", "2+ years"),
        new JobRequirement("QA Tester", java.util.Arrays.asList("Manual Testing", "Selenium", "Bug Reporting", "Automation", "JIRA"), "Bachelor’s in Computer Science or QA Certification", "1+ year"),
        new JobRequirement("Project Manager", java.util.Arrays.asList("Agile", "Scrum", "JIRA", "Communication", "Leadership"), "Bachelor's in Business or IT + PMP Certification", "3+ years"),
        new JobRequirement("Product Manager", java.util.Arrays.asList("Product Lifecycle", "Stakeholder Management", "Scrum", "Roadmapping"), "Bachelor's in Business, CS or related", "3+ years")
        // Add more as needed
    );

    /**
     * Checks a CV file against the requirements for a given job title.
     * @param filePath Path to the CV PDF file
     * @param jobTitle The job title to check against
     * @return A map with keys: skills, qualification, experience, each mapping to a list of missing or found items
     */
    public static java.util.Map<String, Object> checkCV(String filePath, String jobTitle) {
        JobRequirement req = JOB_REQUIREMENTS.stream().filter(j -> j.jobTitle.equalsIgnoreCase(jobTitle)).findFirst().orElse(null);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        if (req == null) {
            result.put("error", "No requirements found for job title: " + jobTitle);
            return result;
        }
        String text = "";
        try {
            text = com.cvreviewapp.utils.PDFReader.extractText(new java.io.File(filePath));
        } catch (Exception e) {
            result.put("error", "Could not read CV PDF: " + e.getMessage());
            return result;
        }
        java.util.List<String> missingSkills = new java.util.ArrayList<>();
        for (String skill : req.skills) {
            if (!text.toLowerCase().contains(skill.toLowerCase())) missingSkills.add(skill);
        }
        boolean hasQualification = text.toLowerCase().contains(req.qualification.toLowerCase());
        boolean hasExperience = text.toLowerCase().contains(req.experience.toLowerCase());
        result.put("missingSkills", missingSkills);
        result.put("hasQualification", hasQualification);
        result.put("hasExperience", hasExperience);
        result.put("jobTitle", jobTitle);
        return result;
    }

    /**
     * Fetches all review criteria from the database.
     */
    public static java.util.List<com.cvreviewapp.models.Criteria> getAllCriteria() {
        java.util.List<com.cvreviewapp.models.Criteria> criteriaList = new java.util.ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id, name, description FROM review_criteria WHERE is_active = TRUE ORDER BY id ASC";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        criteriaList.add(new com.cvreviewapp.models.Criteria(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("description")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while fetching review criteria", e);
        }
        return criteriaList;
    }
} 