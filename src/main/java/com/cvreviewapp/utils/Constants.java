package com.cvreviewapp.utils;

/**
 * Centered constants class for avoiding magic numbers and strings.
 * 
 * Developed by: azihad
 * Contact: azihad783@gmail.com
 * GitHub: AZtheE1
 */
public final class Constants {
    public static final class app {
        public static final String name = "CV-Handler";
    }

    private Constants() {}

    // Roles
    public static final String ROLE_USER = "user";
    public static final String ROLE_ADMIN = "admin";

    // DB Table Names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_SUBMISSIONS = "cv_submissions";
    public static final String TABLE_CRITERIA = "review_criteria";
    public static final String TABLE_REVIEWS = "cv_reviews";
    public static final String TABLE_LOGS = "system_logs";

    // Statuses
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_REVISION_REQUESTED = "REVISION_REQUESTED";

    // Logic Limits
    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    public static final String ALLOWED_EXTENSION = ".pdf";
    public static final int MAX_RETRY_ATTEMPTS = 3;
    public static final int RETRY_DELAY_MS = 2000;

    // Messages
    public static final String MESSAGE_ERROR_UNEXPECTED = "An unexpected error occurred. Please try again.";
    public static final String MESSAGE_SUCCESS_UPLOAD = "CV uploaded successfully.";

    // Session Keys
    public static final String SESSION_USER = "current_user";
}
