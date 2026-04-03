# CV-Handler: Enterprise CV Management & Analysis System

A professional Java Swing desktop application engineered for secure, high-performance CV (resume) submission, management, and dynamic criteria analysis. Built with a modern service-oriented architecture, the system leverages Java 21 features and enterprise-grade security protocols.

---

## 👨‍💻 Author Information
**Developed and Maintained by:**
- **Name:** azihad
- **Email:** [azihad783@gmail.com](mailto:azihad783@gmail.com)
- **GitHub handle:** [AZtheE1](https://github.com/AZtheE1)

> [!IMPORTANT]
> This project is the intellectual property of **azihad (AZtheE1)**. Unauthorized reproduction or redistribution is strictly prohibited.

---

## 🚀 Key Features

### 🛡️ Security & Authentication
- **Production-Grade Auth**: Password hashing using **BCrypt** with high-workload salts.
- **True 2FA**: Time-based One-Time Password (TOTP) integration for secure login verification.
- **Service Layering**: Complete logic decoupling between UI and sensitive database operations.
- **SQL Injection Defense**: 100% PreparedStatements coverage across all data layers.

### 📊 Professional Dashboard System
- **Asynchronous UX**: Non-blocking UI operations using `SwingWorker` with visual loading indicators.
- **User Dashboard**: Secure CV uploads, real-time status tracking, and analysis results view.
- **Admin Dashboard**: System-wide oversight with professional data grids and status management controls.

### 🔍 Intelligence & Processing
- **Dynamic Analysis**: AI-ready criteria matching system that fetches job requirements from a live database.
- **Advanced PDF Extraction**: Optimized text extraction leveraging Apache PDFBox.
- **Automated Schema Management**: First-run database initialization with auto-migration support.

### 📑 Infrastructure
- **Connection Pooling**: High-performance JDBC management via **HikariCP**.
- **Externalized Config**: Secure configuration via `application.properties` (environment decoupled).
- **Modern Data Models**: Efficient data handling using **Java 21 Records**.

---

## 🛠️ Technical Stack
- **Languages**: Java 21
- **Frameworks**: Swing (GUI), FlatLaf (Modern Look & Feel)
- **Database**: MySQL 8.0+
- **Security**: BCrypt, TOTP (de.taimos)
- **Utilities**: Apache PDFBox, HikariCP, Apache Commons Codec

---

## 📦 Setup & Installation

### 1. Prerequisites
- **JDK 21** or later.
- **Maven 3.8+**.
- **MySQL Server** instance running locally or on a server.

### 2. Configuration
Create a file named `src/main/resources/application.properties` (this is ignored by Git for security):
```properties
db.url=jdbc:mysql://localhost:3306/cv_management
db.username=your_mysql_user
db.password=your_mysql_password
db.pool.size=10

smtp.host=smtp.gmail.com
smtp.port=587
smtp.user=your_email@gmail.com
smtp.pass=your_app_password
```

### 3. Build & Run
```bash
# Clone the repository
git clone https://github.com/AZtheE1/CV-Checker.git

# Navigate to directory
cd CV-Checker

# Build the project
mvn clean install

# Launch application
mvn exec:java
```

---

## 📜 Intellectual Property
All rights reserved. Copyright 2026 **azihad (AZtheE1)**. 

---
