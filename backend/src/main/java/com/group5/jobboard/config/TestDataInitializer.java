package com.group5.jobboard.config;

import com.group5.jobboard.entity.*;
import com.group5.jobboard.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class TestDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final JobRepository jobRepository;
    private final PasswordEncoder passwordEncoder;

    public TestDataInitializer(UserRepository userRepository,
                               StudentProfileRepository studentProfileRepository,
                               EmployerProfileRepository employerProfileRepository,
                               JobCategoryRepository jobCategoryRepository,
                               JobRepository jobRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.employerProfileRepository = employerProfileRepository;
        this.jobCategoryRepository = jobCategoryRepository;
        this.jobRepository = jobRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        System.out.println("===== Professional Demo Data Initializer started =====");

        createAdmin();
        createStaff();
        createCategories();
        createStudents();
        createEmployersAndJobs();

        System.out.println("===== Professional Demo Data Initializer finished =====");
    }

    private void createAdmin() {
        createUserIfNotExists(
                "Admin User",
                "admin@test.com",
                "A123456",
                "admin",
                "9999999999",
                "active"
        );
    }

    private void createStaff() {
        createUserIfNotExists(
                "Career Services Staff",
                "staff@test.com",
                "S123456",
                "staff",
                "8888888888",
                "active"
        );

        createUserIfNotExists(
                "Employer Verification Officer",
                "verification.staff@test.com",
                "S123456",
                "staff",
                "8888880001",
                "active"
        );

        createUserIfNotExists(
                "Student Support Officer",
                "student.support@test.com",
                "S123456",
                "staff",
                "8888880002",
                "active"
        );
    }

    private User createUserIfNotExists(String fullName,
                                       String email,
                                       String password,
                                       String role,
                                       String phone,
                                       String status) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setRole(role);
            user.setPhone(phone);
            user.setAccountStatus(status);
            return userRepository.save(user);
        });
    }

    private void createCategories() {
        createCategory("Software Engineering", "Backend, frontend, full-stack and software development roles");
        createCategory("Data Analytics", "Data analysis, BI reporting, SQL, dashboards and insights");
        createCategory("Artificial Intelligence", "Machine learning, AI engineering, automation and intelligent systems");
        createCategory("Cybersecurity", "Security analysis, risk assessment, networking and cloud security");
        createCategory("UX/UI Design", "User experience research, interface design and product design");
        createCategory("Digital Marketing", "SEO, content marketing, social media and campaign operations");
        createCategory("Business Analysis", "Business process analysis, requirements gathering and stakeholder communication");
        createCategory("Finance & Accounting", "Accounting, financial analysis, audit and banking operations");
        createCategory("Human Resources", "Recruitment, HR operations, people analytics and employee relations");
        createCategory("Project Coordination", "Project administration, delivery coordination and operational support");
        createCategory("Customer Success", "Client onboarding, account support and customer operations");
        createCategory("Product Management", "Product strategy, roadmap planning, product operations and market research");
    }

    private void createCategory(String name, String description) {
        if (jobCategoryRepository.findByCategoryName(name).isPresent()) {
            return;
        }

        JobCategory category = new JobCategory();
        category.setCategoryName(name);
        category.setCategoryDescription(description);
        jobCategoryRepository.save(category);
    }

    private void createStudents() {
        Object[][] students = {
                {"Olivia Chen", "olivia.chen@student.test", "0211000001", "Massey University", "Computer Science", "Bachelor", 2026, "Java, Spring Boot, SQL, Git", "Auckland", "internship", "UNDERGRADUATE"},
                {"Liam Patel", "liam.patel@student.test", "0211000002", "University of Auckland", "Data Science", "Master", 2025, "Python, SQL, Power BI, Statistics", "Auckland", "full-time", "GRADUATE"},
                {"Emma Wilson", "emma.wilson@student.test", "0211000003", "Victoria University of Wellington", "Marketing", "Bachelor", 2026, "SEO, Content Writing, Google Analytics", "Wellington", "part-time", "UNDERGRADUATE"},
                {"Noah Thompson", "noah.thompson@student.test", "0211000004", "University of Canterbury", "Software Engineering", "Bachelor", 2025, "React, JavaScript, Node.js, REST API", "Christchurch", "internship", "RECENT_GRADUATE"},
                {"Ava Singh", "ava.singh@student.test", "0211000005", "Massey University", "Business", "Master", 2025, "Business Analysis, Excel, Stakeholder Communication", "Auckland", "full-time", "GRADUATE"},
                {"Ethan Brown", "ethan.brown@student.test", "0211000006", "AUT", "Cybersecurity", "Bachelor", 2026, "Linux, Networking, Security Analysis", "Auckland", "internship", "UNDERGRADUATE"},
                {"Mia Johnson", "mia.johnson@student.test", "0211000007", "University of Waikato", "Finance", "Bachelor", 2025, "Financial Modelling, Excel, Accounting", "Hamilton", "full-time", "RECENT_GRADUATE"},
                {"Lucas Martin", "lucas.martin@student.test", "0211000008", "Massey University", "Information Systems", "Graduate Diploma", 2025, "SQL, Business Systems, Process Mapping", "Remote", "contract", "RECENT_GRADUATE"},
                {"Sophie Lee", "sophie.lee@student.test", "0211000009", "University of Otago", "Design", "Bachelor", 2026, "Figma, UX Research, Prototyping", "Remote", "internship", "UNDERGRADUATE"},
                {"James Anderson", "james.anderson@student.test", "0211000010", "Massey University", "Artificial Intelligence", "Master", 2025, "Machine Learning, Python, NLP, Data Engineering", "Auckland", "full-time", "GRADUATE"},
                {"Isabella Garcia", "isabella.garcia@student.test", "0211000011", "AUT", "Human Resources", "Bachelor", 2026, "Recruitment, HR Operations, Communication", "Auckland", "internship", "UNDERGRADUATE"},
                {"William Davis", "william.davis@student.test", "0211000012", "Victoria University of Wellington", "Project Management", "Graduate Diploma", 2025, "Agile, Jira, Project Coordination", "Wellington", "full-time", "RECENT_GRADUATE"},
                {"Grace Miller", "grace.miller@student.test", "0211000013", "University of Auckland", "Data Analytics", "Master", 2025, "Tableau, SQL, Python, Data Storytelling", "Auckland", "full-time", "GRADUATE"},
                {"Benjamin Taylor", "benjamin.taylor@student.test", "0211000014", "Massey University", "Computer Science", "Bachelor", 2027, "Java, OOP, HTML, CSS", "Auckland", "part-time", "UNDERGRADUATE"},
                {"Charlotte Moore", "charlotte.moore@student.test", "0211000015", "University of Canterbury", "Digital Marketing", "Bachelor", 2026, "Meta Ads, EDM, Campaign Analysis", "Christchurch", "internship", "UNDERGRADUATE"},
                {"Henry Clark", "henry.clark@student.test", "0211000016", "AUT", "Software Engineering", "Bachelor", 2025, "Spring Boot, MySQL, Docker", "Auckland", "full-time", "RECENT_GRADUATE"},
                {"Amelia Scott", "amelia.scott@student.test", "0211000017", "Massey University", "Business Analytics", "Master", 2025, "Power BI, SQL, Excel, Presentation", "Remote", "full-time", "GRADUATE"},
                {"Daniel Young", "daniel.young@student.test", "0211000018", "University of Waikato", "Cybersecurity", "Bachelor", 2026, "Security Monitoring, Cloud, Networking", "Hamilton", "internship", "UNDERGRADUATE"},
                {"Harper King", "harper.king@student.test", "0211000019", "University of Otago", "UX Design", "Bachelor", 2025, "User Research, Wireframes, Figma", "Remote", "contract", "RECENT_GRADUATE"},
                {"Jack Walker", "jack.walker@student.test", "0211000020", "Victoria University of Wellington", "Finance", "Master", 2025, "Financial Analysis, Reporting, Excel", "Wellington", "full-time", "GRADUATE"}
        };

        for (Object[] s : students) {
            User user = createUserIfNotExists(
                    (String) s[0],
                    (String) s[1],
                    "Student123",
                    "student",
                    (String) s[2],
                    "active"
            );

            if (studentProfileRepository.findByUserId(user.getId()).isPresent()) {
                continue;
            }

            StudentProfile profile = new StudentProfile();
            profile.setUserId(user.getId());
            profile.setUniversity((String) s[3]);
            profile.setMajor((String) s[4]);
            profile.setDegreeLevel((String) s[5]);
            profile.setGraduationYear((Integer) s[6]);
            profile.setSkills((String) s[7]);
            profile.setBio("Motivated student seeking practical experience in " + s[4] + ". Strong interest in learning, teamwork and solving real business problems.");
            profile.setPreferredLocation((String) s[8]);
            profile.setPreferredJobType((String) s[9]);
            profile.setStudentType((String) s[10]);

            studentProfileRepository.save(profile);
        }
    }

    private void createEmployersAndJobs() {
        List<JobCategory> categories = jobCategoryRepository.findAll();

        if (categories.isEmpty()) {
            throw new RuntimeException("No job categories found. Please create categories first.");
        }

        createEmployerWithJobs(
                "Auckland Tech Labs",
                "talent@aucklandtechlabs.co.nz",
                "Technology",
                "100-500",
                "https://aucklandtechlabs.co.nz",
                "Auckland",
                "Auckland Tech Labs builds cloud-based software solutions for local and international clients.",
                "Sarah Mitchell",
                "0212000001",
                "approved",
                "/uploads/logos/auckland-tech-labs.png",
                new Object[][]{
                        {"Junior Java Developer", "Software Engineering", "full-time", "hybrid", "Auckland", "Computer Science", "Build backend services using Java and Spring Boot.", "Java, Spring Boot, SQL, REST API", 65000, 82000, "RECENT_GRADUATE", "approved"},
                        {"Software Engineering Intern", "Software Engineering", "internship", "onsite", "Auckland", "Software Engineering", "Support the engineering team with feature development and testing.", "Java, Git, basic SQL, teamwork", 25, 32, "UNDERGRADUATE", "approved"},
                        {"AI Automation Assistant", "Artificial Intelligence", "part-time", "remote", "Remote", "Artificial Intelligence", "Assist with AI workflow testing and automation documentation.", "Python, prompt engineering, basic ML knowledge", 28, 38, "GRADUATE", "pending"}
                }
        );

        createEmployerWithJobs(
                "Wellington Data Insights",
                "careers@wdi.co.nz",
                "Data Analytics",
                "50-100",
                "https://wdi.co.nz",
                "Wellington",
                "Wellington Data Insights provides dashboarding, reporting and analytics consulting services.",
                "Michael Roberts",
                "0212000002",
                "approved",
                "/uploads/logos/wellington-data-insights.png",
                new Object[][]{
                        {"Graduate Data Analyst", "Data Analytics", "full-time", "hybrid", "Wellington", "Data Science", "Analyse business data and build reporting dashboards for clients.", "SQL, Power BI, Excel, Python", 62000, 78000, "GRADUATE", "approved"},
                        {"Business Intelligence Intern", "Data Analytics", "internship", "onsite", "Wellington", "Information Systems", "Support BI consultants with data preparation and dashboard testing.", "SQL, Excel, communication", 24, 30, "UNDERGRADUATE", "approved"},
                        {"Reporting Analyst Contractor", "Data Analytics", "contract", "remote", "Remote", "Business Analytics", "Create weekly operational reporting packs.", "Tableau, Power BI, stakeholder communication", 45, 65, "ALL", "pending"}
                }
        );

        createEmployerWithJobs(
                "KiwiBank Digital",
                "graduates@kiwibankdigital.co.nz",
                "Finance",
                "500+",
                "https://kiwibankdigital.co.nz",
                "Auckland",
                "KiwiBank Digital focuses on financial technology, customer experience and secure banking systems.",
                "Rebecca Turner",
                "0212000003",
                "approved",
                "/uploads/logos/kiwibank-digital.png",
                new Object[][]{
                        {"Finance Graduate Analyst", "Finance & Accounting", "full-time", "onsite", "Auckland", "Finance", "Support financial reporting, forecasting and operational analysis.", "Excel, financial modelling, accounting knowledge", 60000, 74000, "GRADUATE", "approved"},
                        {"Risk Operations Assistant", "Business Analysis", "part-time", "hybrid", "Auckland", "Business", "Assist risk team with documentation, data checks and process improvement.", "Excel, attention to detail, communication", 26, 34, "UNDERGRADUATE", "approved"},
                        {"Cybersecurity Graduate", "Cybersecurity", "full-time", "hybrid", "Auckland", "Cybersecurity", "Support monitoring, incident response and security documentation.", "Networking, Linux, cybersecurity fundamentals", 68000, 85000, "RECENT_GRADUATE", "pending"}
                }
        );

        createEmployerWithJobs(
                "Southern Creative Studio",
                "jobs@southerncreative.co.nz",
                "Design",
                "20-50",
                "https://southerncreative.co.nz",
                "Christchurch",
                "Southern Creative Studio designs digital experiences for education, retail and technology brands.",
                "Emily Watson",
                "0212000004",
                "approved",
                "/uploads/logos/southern-creative-studio.png",
                new Object[][]{
                        {"UX Research Intern", "UX/UI Design", "internship", "hybrid", "Christchurch", "Design", "Conduct user interviews, competitor research and usability notes.", "Figma, UX research, communication", 24, 31, "UNDERGRADUATE", "approved"},
                        {"Junior UI Designer", "UX/UI Design", "full-time", "onsite", "Christchurch", "Design", "Create interface designs, prototypes and design system components.", "Figma, prototyping, visual design", 56000, 70000, "RECENT_GRADUATE", "approved"},
                        {"Digital Content Designer", "Digital Marketing", "contract", "remote", "Remote", "Marketing", "Design social and email campaign assets for client projects.", "Canva, Adobe, EDM, content layout", 35, 55, "ALL", "rejected"}
                }
        );

        createEmployerWithJobs(
                "NorthStar Marketing Group",
                "recruitment@northstarmarketing.co.nz",
                "Marketing",
                "50-100",
                "https://northstarmarketing.co.nz",
                "Auckland",
                "NorthStar Marketing Group helps growing brands with digital campaigns, CRM and customer acquisition.",
                "Daniel Foster",
                "0212000005",
                "approved",
                "/uploads/logos/northstar-marketing.png",
                new Object[][]{
                        {"Digital Marketing Coordinator", "Digital Marketing", "full-time", "hybrid", "Auckland", "Marketing", "Coordinate email campaigns, paid media reporting and content calendars.", "SEO, EDM, Google Analytics, Meta Ads", 56000, 72000, "RECENT_GRADUATE", "approved"},
                        {"Marketing Operations Intern", "Digital Marketing", "internship", "onsite", "Auckland", "Marketing", "Support campaign scheduling, reporting and competitor research.", "Excel, content writing, social media", 24, 30, "UNDERGRADUATE", "approved"},
                        {"CRM Campaign Assistant", "Digital Marketing", "part-time", "remote", "Remote", "Business", "Help build segmentation and automation workflows.", "CRM, email marketing, data analysis", 28, 38, "ALL", "pending"}
                }
        );

        createEmployerWithJobs(
                "Hamilton Health Systems",
                "people@hamiltonhealthsystems.co.nz",
                "Healthcare",
                "100-500",
                "https://hamiltonhealthsystems.co.nz",
                "Hamilton",
                "Hamilton Health Systems develops digital tools for healthcare operations and patient support.",
                "Rachel Green",
                "0212000006",
                "pending",
                "/uploads/logos/hamilton-health-systems.png",
                new Object[][]{
                        {"IT Support Graduate", "Software Engineering", "full-time", "onsite", "Hamilton", "Information Systems", "Support internal systems, tickets and documentation.", "Troubleshooting, SQL basics, communication", 54000, 66000, "RECENT_GRADUATE", "approved"},
                        {"Healthcare Data Assistant", "Data Analytics", "part-time", "hybrid", "Hamilton", "Data Science", "Assist with data cleaning and operational reporting.", "Excel, SQL, data privacy awareness", 26, 35, "UNDERGRADUATE", "approved"},
                        {"Systems Analyst", "Business Analysis", "full-time", "hybrid", "Hamilton", "Information Systems", "Document requirements and improve business workflows.", "Business analysis, process mapping, communication", 65000, 83000, "GRADUATE", "pending"}
                }
        );

        createEmployerWithJobs(
                "CloudWorks NZ",
                "careers@cloudworks.nz",
                "Technology",
                "20-50",
                "https://cloudworks.nz",
                "Remote",
                "CloudWorks NZ provides cloud migration, DevOps and managed infrastructure services.",
                "Tom Harris",
                "0212000007",
                "approved",
                "/uploads/logos/cloudworks-nz.png",
                new Object[][]{
                        {"Cloud Support Intern", "Software Engineering", "internship", "remote", "Remote", "Computer Science", "Support cloud engineers with monitoring and documentation.", "Linux, cloud basics, troubleshooting", 25, 32, "UNDERGRADUATE", "approved"},
                        {"Junior DevOps Engineer", "Software Engineering", "full-time", "remote", "Remote", "Software Engineering", "Assist with CI/CD, deployment and infrastructure automation.", "Docker, GitHub Actions, Linux, scripting", 70000, 90000, "RECENT_GRADUATE", "approved"},
                        {"Security Operations Analyst", "Cybersecurity", "full-time", "remote", "Remote", "Cybersecurity", "Monitor security alerts and document response procedures.", "Security monitoring, networking, Linux", 72000, 92000, "GRADUATE", "pending"}
                }
        );

        createEmployerWithJobs(
                "Pacific Retail Group",
                "hr@pacificretail.co.nz",
                "Retail",
                "500+",
                "https://pacificretail.co.nz",
                "Auckland",
                "Pacific Retail Group operates multi-channel retail stores and e-commerce platforms across New Zealand.",
                "Jessica Morgan",
                "0212000008",
                "approved",
                "/uploads/logos/pacific-retail-group.png",
                new Object[][]{
                        {"E-commerce Operations Assistant", "Business Analysis", "full-time", "onsite", "Auckland", "Business", "Support online product operations, reporting and process improvement.", "Excel, Shopify, communication", 55000, 68000, "RECENT_GRADUATE", "approved"},
                        {"Customer Success Intern", "Customer Success", "internship", "hybrid", "Auckland", "Business", "Support customer onboarding and service documentation.", "Communication, CRM, problem solving", 23, 29, "UNDERGRADUATE", "approved"},
                        {"Product Operations Coordinator", "Product Management", "full-time", "hybrid", "Auckland", "Information Systems", "Coordinate product updates, testing and stakeholder communication.", "Product operations, Excel, Jira", 62000, 78000, "ALL", "pending"}
                }
        );

        createEmployerWithJobs(
                "Canterbury Engineering Solutions",
                "talent@ces.co.nz",
                "Consulting",
                "50-100",
                "https://ces.co.nz",
                "Christchurch",
                "Canterbury Engineering Solutions provides technical consulting and project delivery support.",
                "Andrew Bell",
                "0212000009",
                "approved",
                "/uploads/logos/canterbury-engineering-solutions.png",
                new Object[][]{
                        {"Project Coordinator Graduate", "Project Coordination", "full-time", "onsite", "Christchurch", "Business", "Coordinate project schedules, documentation and internal reporting.", "Project coordination, Excel, communication", 58000, 72000, "RECENT_GRADUATE", "approved"},
                        {"Business Analyst Intern", "Business Analysis", "internship", "hybrid", "Christchurch", "Business", "Assist consultants with requirements gathering and process mapping.", "Business analysis, documentation, stakeholder communication", 24, 32, "UNDERGRADUATE", "approved"},
                        {"Graduate Consultant", "Business Analysis", "full-time", "hybrid", "Christchurch", "Business", "Support client projects, research and solution documentation.", "Research, analysis, presentation", 65000, 80000, "GRADUATE", "pending"}
                }
        );

        createEmployerWithJobs(
                "GreenFuture Energy",
                "recruitment@greenfutureenergy.co.nz",
                "Energy",
                "100-500",
                "https://greenfutureenergy.co.nz",
                "Wellington",
                "GreenFuture Energy works on renewable energy analytics, project planning and sustainability reporting.",
                "Laura Evans",
                "0212000010",
                "approved",
                "/uploads/logos/greenfuture-energy.png",
                new Object[][]{
                        {"Sustainability Data Analyst", "Data Analytics", "full-time", "hybrid", "Wellington", "Data Science", "Analyse energy usage and sustainability metrics.", "SQL, Excel, Power BI, sustainability interest", 64000, 82000, "GRADUATE", "approved"},
                        {"Project Support Intern", "Project Coordination", "internship", "onsite", "Wellington", "Business", "Support renewable project documentation and coordination.", "Excel, communication, project admin", 24, 30, "UNDERGRADUATE", "approved"},
                        {"Energy Reporting Assistant", "Data Analytics", "part-time", "remote", "Remote", "Data Analytics", "Prepare weekly reporting and data quality checks.", "Excel, data cleaning, reporting", 28, 40, "ALL", "approved"}
                }
        );
    }

    private void createEmployerWithJobs(String companyName,
                                        String email,
                                        String industry,
                                        String companySize,
                                        String website,
                                        String location,
                                        String description,
                                        String contactPerson,
                                        String phone,
                                        String verificationStatus,
                                        String logoUrl,
                                        Object[][] jobs) {
        User employer = createUserIfNotExists(
                contactPerson,
                email,
                "Employer123",
                "employer",
                phone,
                "active"
        );

        if (employerProfileRepository.findByUserId(employer.getId()).isEmpty()) {
            EmployerProfile profile = new EmployerProfile();
            profile.setUserId(employer.getId());
            profile.setCompanyName(companyName);
            profile.setIndustry(industry);
            profile.setCompanySize(companySize);
            profile.setWebsite(website);
            profile.setLocation(location);
            profile.setCompanyDescription(description);
            profile.setContactPerson(contactPerson);
            profile.setContactEmail(email);
            profile.setVerificationStatus(verificationStatus);
            profile.setLogoUrl(logoUrl);

            employerProfileRepository.save(profile);
        }

        long existingJobCount = jobRepository.findByEmployerId(employer.getId()).size();
        if (existingJobCount > 0) {
            return;
        }

        for (Object[] j : jobs) {
            JobCategory category = jobCategoryRepository.findByCategoryName((String) j[1])
                    .orElseThrow(() -> new RuntimeException("Category not found: " + j[1]));

            Job job = new Job();
            job.setEmployerId(employer.getId());
            job.setTitle((String) j[0]);
            job.setCategoryId(category.getId());
            job.setDescription((String) j[6]);
            job.setRequirements((String) j[7]);
            job.setEmploymentType((String) j[2]);
            job.setWorkMode((String) j[3]);
            job.setLocation((String) j[4]);
            job.setFieldOfStudy((String) j[5]);
            job.setSalaryMin(BigDecimal.valueOf(((Number) j[8]).doubleValue()));
            job.setSalaryMax(BigDecimal.valueOf(((Number) j[9]).doubleValue()));
            job.setTargetStudentType((String) j[10]);
            job.setStatus((String) j[11]);
            job.setDeadline(LocalDateTime.now().plusDays(30 + (int) (Math.random() * 60)));

            jobRepository.save(job);
        }
    }
}