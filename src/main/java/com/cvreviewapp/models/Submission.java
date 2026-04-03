package com.cvreviewapp.models;

import java.sql.Timestamp;

public class Submission {
    private int id;
    private int userId;
    private String title;
    private String description;
    private String filePath;
    private String fileName;
    private long fileSize;
    private String fileType;
    private String status;
    private Timestamp submissionDate;
    private Timestamp updatedAt;
    private Integer reviewerId;
    private Timestamp reviewDate;
    private String notes;
    private String jobTitle;

    public Submission(int id, int userId, String title, String description, String filePath, String fileName, long fileSize, String fileType, String status, Timestamp submissionDate, Timestamp updatedAt, Integer reviewerId, Timestamp reviewDate, String notes, String jobTitle) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.status = status;
        this.submissionDate = submissionDate;
        this.updatedAt = updatedAt;
        this.reviewerId = reviewerId;
        this.reviewDate = reviewDate;
        this.notes = notes;
        this.jobTitle = jobTitle;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getFilePath() { return filePath; }
    public String getFileName() { return fileName; }
    public long getFileSize() { return fileSize; }
    public String getFileType() { return fileType; }
    public String getStatus() { return status; }
    public Timestamp getSubmissionDate() { return submissionDate; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public Integer getReviewerId() { return reviewerId; }
    public Timestamp getReviewDate() { return reviewDate; }
    public String getNotes() { return notes; }
    public String getJobTitle() { return jobTitle; }

    @Override
    public String toString() {
        return "Submission{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", fileType='" + fileType + '\'' +
                ", status='" + status + '\'' +
                ", submissionDate=" + submissionDate +
                ", updatedAt=" + updatedAt +
                ", reviewerId=" + reviewerId +
                ", reviewDate=" + reviewDate +
                ", notes='" + notes + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                '}';
    }
} 