package com.cvreviewapp.models;

import java.sql.Timestamp;

/**
 * CV Submission data container using Java 21 Records.
 * 
 * Developed by: azihad
 * Contact: azihad783@gmail.com
 * GitHub: AZtheE1
 */
public record Submission(
    int id,
    int userId,
    String title,
    String description,
    String filePath,
    String fileName,
    long fileSize,
    String fileType,
    String status,
    Timestamp submissionDate,
    Timestamp updatedAt,
    Integer reviewerId,
    Timestamp reviewDate,
    String notes,
    String jobTitle
) {}