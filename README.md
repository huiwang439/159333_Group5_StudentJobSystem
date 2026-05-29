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
* AI assistant support
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
* Bootstrap
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
│   └── common
│
└── src/main/resources
    ├── static
    │      ├── application.properties
    │      ├── student
    │      ├── employer
    │      ├── admin
    │      └── index
    └── sql

How to Run the Project
1. Clone the Repository
git clone https://github.com/huiwang439/159333_Group5_StudentJobSystem.git
2. Open the Project
Open the project using: IntelliJ IDEA (recommended)
3. Configure MySQL
Start MySQL service 
Create the database
   CREATE DATABASE jobboard
   CHARACTER SET utf8mb4
   COLLATE utf8mb4_unicode_ci;
   SHOW DATABASES;
Update database credentials in `application.properties`
4. Run Spring Boot
Run the main application class: JobBoardApplication.java
5. Open in Browser
http://localhost:8080

Team Collaboration
This project uses Git and GitHub for version control and collaborative development.

