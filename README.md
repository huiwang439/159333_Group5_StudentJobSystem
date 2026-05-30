Internship and Job Board for Students
A full-stack web application designed to help university students and recent graduates search for internships and job opportunities, while allowing employers and administrators to manage recruitment activities efficiently.

Project Overview
This system provides a centralized internship and job platform where:
* Students can search and apply for jobs
* Employers can post and manage job listings
* Career staff can monitor student employment activities
* Administrators can manage the entire platform
The project focuses on improving employability services through a modern, user-friendly, and responsive web application.

Features
1. Student Features
* User registration and login
* Browse internship and job listings
* Search and filter jobs
* Save favorite jobs
* Apply for jobs online
* Upload resumes and portfolios
* Track application status
* Receive notifications
* AI Career Assistant for students
2. Employer Features
* Employer registration and login
* Create and manage job postings
* View student applications
* Review applicant profiles
* Manage recruitment workflow
3. Career Staff Features
* Dashboard for monitoring applications
* View student employment statistics
* Generate reports and analytics
* Support student career development
4. Admin Features
* Platform management dashboard
* User management
* Job moderation
* Fraud/risk detection
* Analytics overview
* Employer verification

Technology Stack
1. Frontend
* HTML5
* CSS3
* JavaScript
2. Backend
* Java
* Spring Boot
* Spring MVC
* Spring Security
* REST API
3. Database
* MySQL
4. Tools & Platforms
* IntelliJ IDEA
* Git & GitHub
* Maven

Project Structure
backend/
├── src/main/java/com/group5/jobboard
│   ├── controller
│   ├── service
│   ├── repository
│   ├── entity
│   ├── dto
│   ├── config
│   ├── common
│   └── JobboardApplication.java
└── src/main/resources
    ├── static
    │      ├── student
    │      ├── employer
    │      ├── admin
    │      └── index
    ├── sql
    └── application.properties

How to Run the Project
1. Requirements:
- Java 17
- Maven
- MySQL 8.0 or above
- IntelliJ IDEA
2. Clone the Repository
git clone https://github.com/huiwang439/159333_Group5_StudentJobSystem.git
3. Open the Project
Open the project using: IntelliJ IDEA (recommended)
Switch to the 'develop' branch
4. Configure MySQL
Start MySQL service 
Create the database
   CREATE DATABASE jobboard
   CHARACTER SET utf8mb4
   COLLATE utf8mb4_unicode_ci;
   SHOW DATABASES;
   USE jobboard;
Change the database username and password in `backend/src/main/resources/application.properties`
5. Run Spring Boot
Run backend/src/main/java/com/group5/jobboard/JobboardApplication.java in IntelliJ IDEA.
6. Open in Browser
   http://localhost:8080/index/index.html
7. The test accounts are as follows:
   Admin: admin@test.com / A123456
   Staff: staff@test.com / S123456
   Student: student001@student.test /Student123
   Employer: employer001@company.test /Employer123

Team Collaboration
This project uses Git and GitHub for version control and collaborative development.

