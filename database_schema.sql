-- CV Management App Database Schema
-- This script creates all necessary tables for the CV Management Application

-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS cv_management;
USE cv_management;

-- Users table - stores user account information
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role ENUM('USER', 'ADMIN', 'REVIEWER') DEFAULT 'USER',
    phone VARCHAR(20),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    two_factor_secret VARCHAR(255) NULL
);

-- CV submissions table - stores uploaded CV files and metadata
CREATE TABLE IF NOT EXISTS cv_submissions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    file_path VARCHAR(500) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    status ENUM('PENDING', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'ARCHIVED') DEFAULT 'PENDING',
    submission_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    reviewer_id INT NULL,
    review_date TIMESTAMP NULL,
    notes TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Review criteria table - stores evaluation criteria for CVs
CREATE TABLE IF NOT EXISTS review_criteria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    max_score INT DEFAULT 10,
    weight DECIMAL(3,2) DEFAULT 1.00,
    category VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- CV reviews table - stores individual review scores and comments
CREATE TABLE IF NOT EXISTS cv_reviews (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cv_submission_id INT NOT NULL,
    reviewer_id INT NOT NULL,
    criteria_id INT NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    comments TEXT,
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cv_submission_id) REFERENCES cv_submissions(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (criteria_id) REFERENCES review_criteria(id) ON DELETE CASCADE,
    UNIQUE KEY unique_review (cv_submission_id, reviewer_id, criteria_id)
);

-- Review sessions table - tracks review sessions and overall scores
CREATE TABLE IF NOT EXISTS review_sessions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cv_submission_id INT NOT NULL,
    reviewer_id INT NOT NULL,
    overall_score DECIMAL(5,2),
    overall_comments TEXT,
    review_status ENUM('IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'IN_PROGRESS',
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    FOREIGN KEY (cv_submission_id) REFERENCES cv_submissions(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_session (cv_submission_id, reviewer_id)
);

-- User sessions table - tracks user login sessions
CREATE TABLE IF NOT EXISTS user_sessions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    session_token VARCHAR(255) UNIQUE NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- System logs table - stores application logs
CREATE TABLE IF NOT EXISTS system_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    action VARCHAR(100) NOT NULL,
    details TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    log_level ENUM('INFO', 'WARNING', 'ERROR', 'DEBUG') DEFAULT 'INFO',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Insert default admin user (password: admin123)
INSERT INTO users (username, email, password_hash, first_name, last_name, role) 
VALUES ('admin', 'admin@cvmanagement.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'System', 'Administrator', 'ADMIN')
ON DUPLICATE KEY UPDATE id=id;

-- Insert default review criteria
INSERT INTO review_criteria (name, description, max_score, weight, category) VALUES
('Education', 'Educational background and qualifications', 10, 1.00, 'ACADEMIC'),
('Experience', 'Relevant work experience and skills', 10, 1.20, 'PROFESSIONAL'),
('Skills', 'Technical and soft skills', 10, 1.00, 'SKILLS'),
('Presentation', 'CV formatting and presentation quality', 10, 0.80, 'PRESENTATION'),
('Relevance', 'Relevance to job requirements', 10, 1.10, 'RELEVANCE'),
('Communication', 'Written communication skills', 10, 0.90, 'COMMUNICATION')
ON DUPLICATE KEY UPDATE id=id;

-- Create indexes for better performance
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_cv_submissions_user_id ON cv_submissions(user_id);
CREATE INDEX idx_cv_submissions_status ON cv_submissions(status);
CREATE INDEX idx_cv_submissions_date ON cv_submissions(submission_date);
CREATE INDEX idx_cv_reviews_submission_id ON cv_reviews(cv_submission_id);
CREATE INDEX idx_cv_reviews_reviewer_id ON cv_reviews(reviewer_id);
CREATE INDEX idx_user_sessions_token ON user_sessions(session_token);
CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_system_logs_user_id ON system_logs(user_id);
CREATE INDEX idx_system_logs_created_at ON system_logs(created_at); 