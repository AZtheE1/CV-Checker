# CV Management App

A Java Swing desktop application for managing CV (resume) submissions and reviews, with MySQL database integration and PDF handling via PDFBox.

## Features
- User/Admin authentication with password hashing and simulated 2FA
- User dashboard for CV upload and status tracking
- Admin dashboard for reviewing submissions and managing criteria
- PDF extraction using PDFBox
- MySQL database for users, submissions, and criteria

## Setup Instructions

### Prerequisites
- Java 21+
- Maven
- MySQL Server

### Database Setup
1. Create a database named `cv_management`.
2. Run the following SQL schema:

```sql
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL,
    role ENUM('user', 'admin') NOT NULL,
    email VARCHAR(100) NOT NULL
);

CREATE TABLE criteria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT
);

CREATE TABLE submissions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Build & Run

1. Clone the repository.
2. Configure your MySQL credentials in `DBConnection.java`.
3. Build the project:
   ```sh
   mvn clean package
   ```
4. Run the application:
   ```sh
   mvn exec:java
   ```

## Libraries
- [Apache PDFBox](https://pdfbox.apache.org/)
- [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/)

## Directory Structure
```
src/main/java/com/cvreviewapp/
  Main.java
  LoginUI.java
  AdminDashboard.java
  UserDashboard.java
  models/
    User.java
    Submission.java
    Criteria.java
  utils/
    DBConnection.java
    PDFReader.java
libraries/ (for external JARs)
README.md
```

## Notes
- For demo purposes, 2FA is simulated with a static code.
- Passwords are hashed using SHA-256.
- Error handling and logging are implemented for user actions and system errors.
