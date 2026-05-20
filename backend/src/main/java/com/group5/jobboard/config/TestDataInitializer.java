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
        System.out.println("===== Large Demo Data Initializer started =====");

        createAdmin();
        createStaff();
        createCategories();
        createStudents(60);
        createEmployersAndJobs(36);

        System.out.println("===== Large Demo Data Initializer finished =====");
    }

    private void createAdmin() {
        createUserIfNotExists("Admin User", "admin@test.com", "A123456", "admin", "9999999999", "active");
    }

    private void createStaff() {
        createUserIfNotExists("Career Services Staff", "staff@test.com", "S123456", "staff", "8888888888", "active");
        createUserIfNotExists("Employer Verification Officer", "verification.staff@test.com", "S123456", "staff", "8888880001", "active");
        createUserIfNotExists("Student Support Officer", "student.support@test.com", "S123456", "staff", "8888880002", "active");
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
        createCategory("Artificial Intelligence", "Machine learning, AI engineering and intelligent automation");
        createCategory("Cybersecurity", "Security analysis, networking and cloud security");
        createCategory("UX/UI Design", "UX research, interface design and product design");
        createCategory("Digital Marketing", "SEO, content marketing, EDM, social media and campaign operations");
        createCategory("Business Analysis", "Business process analysis, requirements and stakeholder communication");
        createCategory("Finance & Accounting", "Accounting, financial analysis, audit and banking operations");
        createCategory("Human Resources", "Recruitment, HR operations and people analytics");
        createCategory("Project Coordination", "Project administration, coordination and operational support");
        createCategory("Customer Success", "Client onboarding, account support and customer operations");
        createCategory("Product Management", "Product strategy, roadmap planning and product operations");
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

    private void createStudents(int count) {
        String[] firstNames = {"Olivia", "Liam", "Emma", "Noah", "Ava", "Ethan", "Mia", "Lucas", "Sophie", "James", "Isabella", "William", "Grace", "Benjamin", "Charlotte", "Henry", "Amelia", "Daniel", "Harper", "Jack"};
        String[] lastNames = {"Chen", "Patel", "Wilson", "Thompson", "Singh", "Brown", "Johnson", "Martin", "Lee", "Anderson", "Garcia", "Davis", "Miller", "Taylor", "Moore", "Clark", "Scott", "Young", "King", "Walker"};
        String[] universities = {"Massey University", "University of Auckland", "AUT", "Victoria University of Wellington", "University of Canterbury", "University of Waikato", "University of Otago"};
        String[] majors = {"Computer Science", "Data Science", "Software Engineering", "Information Systems", "Artificial Intelligence", "Cybersecurity", "Business", "Marketing", "Finance", "Design", "Human Resources", "Project Management"};
        String[] degrees = {"Bachelor", "Master", "Graduate Diploma"};
        String[] locations = {"Auckland", "Wellington", "Christchurch", "Hamilton", "Remote"};
        String[] jobTypes = {"internship", "full-time", "part-time", "contract"};
        String[] studentTypes = {"UNDERGRADUATE", "GRADUATE", "RECENT_GRADUATE"};
        String[] skills = {
                "Java, Spring Boot, SQL, Git",
                "Python, SQL, Power BI, Statistics",
                "React, JavaScript, HTML, CSS",
                "Figma, UX Research, Prototyping",
                "SEO, EDM, Google Analytics, Meta Ads",
                "Excel, Financial Modelling, Reporting",
                "Linux, Networking, Security Monitoring",
                "Business Analysis, Jira, Process Mapping",
                "Machine Learning, Python, NLP",
                "Recruitment, HR Operations, Communication"
        };

        for (int i = 1; i <= count; i++) {
            String fullName = firstNames[(i - 1) % firstNames.length] + " " + lastNames[(i - 1) % lastNames.length] + " " + i;
            String email = "demo.student" + String.format("%03d", i) + "@student.test";

            User user = createUserIfNotExists(
                    fullName,
                    email,
                    "Student123",
                    "student",
                    "02110" + String.format("%05d", i),
                    i % 25 == 0 ? "banned" : "active"
            );

            if (studentProfileRepository.findByUserId(user.getId()).isPresent()) {
                continue;
            }

            String major = majors[(i - 1) % majors.length];

            StudentProfile profile = new StudentProfile();
            profile.setUserId(user.getId());
            profile.setUniversity(universities[(i - 1) % universities.length]);
            profile.setMajor(major);
            profile.setDegreeLevel(degrees[(i - 1) % degrees.length]);
            profile.setGraduationYear(2025 + (i % 4));
            profile.setSkills(skills[(i - 1) % skills.length]);
            profile.setBio("Motivated " + major + " student seeking practical career opportunities. Strong interest in teamwork, learning and solving real business problems.");
            profile.setPreferredLocation(locations[(i - 1) % locations.length]);
            profile.setPreferredJobType(jobTypes[(i - 1) % jobTypes.length]);
            profile.setStudentType(studentTypes[(i - 1) % studentTypes.length]);

            studentProfileRepository.save(profile);
        }
    }

    private void createEmployersAndJobs(int employerCount) {
        String[] companies = {
                "Auckland Tech Labs", "Wellington Data Insights", "KiwiBank Digital", "Southern Creative Studio",
                "NorthStar Marketing Group", "Hamilton Health Systems", "CloudWorks NZ", "Pacific Retail Group",
                "Canterbury Engineering Solutions", "GreenFuture Energy", "Harbour Finance Partners", "BrightPath Education",
                "SilverLine Consulting", "Metro Software Group", "BluePeak Security", "ClearView Analytics",
                "RetailX Digital", "FutureWorks AI", "PeopleFirst HR", "LaunchPad Product Studio",
                "Oceanic Cloud Services", "Capital Business Solutions", "Summit Accounting", "Nexus Customer Success",
                "RedBridge Marketing", "Digital Orchard", "TechNova Systems", "InsightWorks NZ",
                "Alpha Project Partners", "Urban Mobility Tech", "EcoSmart Consulting", "Southern Data Lab",
                "VectorLearn Education", "Meridian Retail Tech", "SecureNet Operations", "Aotearoa Product Group"
        };

        String[] industries = {"Technology", "Data Analytics", "Finance", "Design", "Marketing", "Healthcare", "Retail", "Consulting", "Energy", "Education"};
        String[] locations = {"Auckland", "Wellington", "Christchurch", "Hamilton", "Remote"};
        String[] companySizes = {"20-50", "50-100", "100-500", "500+"};
        String[] statuses = {"approved", "approved", "approved", "pending"};

        String[][] jobTemplates = {
                {"Junior Java Developer", "Software Engineering", "full-time", "hybrid", "Computer Science", "Build backend services using Java and Spring Boot.", "Java, Spring Boot, SQL, REST API", "RECENT_GRADUATE"},
                {"Software Engineering Intern", "Software Engineering", "internship", "onsite", "Software Engineering", "Support feature development, testing and documentation.", "Java, Git, SQL, teamwork", "UNDERGRADUATE"},
                {"Graduate Data Analyst", "Data Analytics", "full-time", "hybrid", "Data Science", "Analyse business data and build reporting dashboards.", "SQL, Power BI, Excel, Python", "GRADUATE"},
                {"Business Intelligence Intern", "Data Analytics", "internship", "onsite", "Information Systems", "Support BI reporting, data preparation and dashboard testing.", "SQL, Excel, communication", "UNDERGRADUATE"},
                {"AI Automation Assistant", "Artificial Intelligence", "part-time", "remote", "Artificial Intelligence", "Assist with AI workflow testing and automation documentation.", "Python, ML basics, prompt engineering", "GRADUATE"},
                {"Cybersecurity Analyst", "Cybersecurity", "full-time", "hybrid", "Cybersecurity", "Monitor security alerts and assist with incident documentation.", "Networking, Linux, security fundamentals", "RECENT_GRADUATE"},
                {"UX Research Intern", "UX/UI Design", "internship", "hybrid", "Design", "Conduct user research, interviews and usability notes.", "Figma, UX research, communication", "UNDERGRADUATE"},
                {"Junior UI Designer", "UX/UI Design", "full-time", "onsite", "Design", "Create UI designs, prototypes and design system components.", "Figma, prototyping, visual design", "RECENT_GRADUATE"},
                {"Digital Marketing Coordinator", "Digital Marketing", "full-time", "hybrid", "Marketing", "Coordinate EDM, paid media reporting and content calendars.", "SEO, EDM, Google Analytics, Meta Ads", "RECENT_GRADUATE"},
                {"Marketing Operations Intern", "Digital Marketing", "internship", "onsite", "Marketing", "Support campaign scheduling, reporting and competitor research.", "Excel, content writing, social media", "UNDERGRADUATE"},
                {"Business Analyst Intern", "Business Analysis", "internship", "hybrid", "Business", "Assist with requirements gathering and process mapping.", "Business analysis, documentation, communication", "UNDERGRADUATE"},
                {"Graduate Business Analyst", "Business Analysis", "full-time", "hybrid", "Business", "Document requirements and support business process improvement.", "Excel, Jira, stakeholder communication", "GRADUATE"},
                {"Finance Graduate Analyst", "Finance & Accounting", "full-time", "onsite", "Finance", "Support reporting, forecasting and operational analysis.", "Excel, accounting, financial modelling", "GRADUATE"},
                {"HR Recruitment Assistant", "Human Resources", "part-time", "hybrid", "Human Resources", "Support recruitment coordination and HR documentation.", "Communication, recruitment, Excel", "ALL"},
                {"Project Coordinator Graduate", "Project Coordination", "full-time", "onsite", "Project Management", "Coordinate schedules, documentation and project reporting.", "Agile, Jira, Excel, communication", "RECENT_GRADUATE"},
                {"Customer Success Intern", "Customer Success", "internship", "hybrid", "Business", "Support onboarding, customer communication and CRM updates.", "CRM, communication, problem solving", "UNDERGRADUATE"},
                {"Product Operations Coordinator", "Product Management", "full-time", "hybrid", "Information Systems", "Coordinate product updates, testing and stakeholder communication.", "Product operations, Jira, Excel", "ALL"}
        };

        int totalJobsCreated = 0;

        for (int i = 1; i <= employerCount; i++) {
            String companyName = companies[(i - 1) % companies.length];
            String email = "demo.employer" + String.format("%03d", i) + "@company.test";
            String[] employerFirstNames = {
                    "Michael", "Sarah", "David", "Emily", "James",
                    "Jessica", "Daniel", "Sophia", "Matthew", "Olivia",
                    "Andrew", "Emma", "Ryan", "Chloe", "Nathan",
                    "Grace", "Ethan", "Mia", "Lucas", "Hannah",
                    "Benjamin", "Lily", "William", "Ella", "Noah",
                    "Charlotte", "Jack", "Amelia", "Henry", "Zoe"
            };

            String[] employerLastNames = {
                    "Wilson", "Taylor", "Johnson", "Brown", "Lee",
                    "Martin", "Clark", "Walker", "Hall", "Young",
                    "King", "Scott", "Green", "Baker", "Adams",
                    "Hill", "Roberts", "Turner", "Phillips", "Campbell",
                    "Parker", "Evans", "Edwards", "Collins", "Stewart",
                    "Morris", "Rogers", "Cook", "Morgan", "Bell"
            };

            String contactPerson =
                    employerFirstNames[(i - 1) % employerFirstNames.length]
                            + " "
                            + employerLastNames[(i - 1) % employerLastNames.length]
                            + " "
                            + i;

            User employer = createUserIfNotExists(
                    contactPerson,
                    email,
                    "Employer123",
                    "employer",
                    "02120" + String.format("%05d", i),
                    "active"
            );

            if (employerProfileRepository.findByUserId(employer.getId()).isEmpty()) {
                EmployerProfile profile = new EmployerProfile();
                profile.setUserId(employer.getId());
                profile.setCompanyName(companyName);
                profile.setIndustry(industries[(i - 1) % industries.length]);
                profile.setCompanySize(companySizes[(i - 1) % companySizes.length]);
                profile.setWebsite("https://" + companyName.toLowerCase().replace(" ", "-") + ".co.nz");
                profile.setLocation(locations[(i - 1) % locations.length]);
                profile.setCompanyDescription(companyName + " provides professional services and entry-level career opportunities across New Zealand.");
                profile.setContactPerson(contactPerson);
                profile.setContactEmail(email);
                profile.setVerificationStatus(statuses[(i - 1) % statuses.length]);
                profile.setLogoUrl("/uploads/logos/company-" + String.format("%03d", i) + ".png");

                employerProfileRepository.save(profile);
            }

            if (!jobRepository.findByEmployerId(employer.getId()).isEmpty()) {
                continue;
            }

            int jobsForThisEmployer = i <= 6 ? 5 : 4;

            for (int j = 1; j <= jobsForThisEmployer; j++) {
                String[] template = jobTemplates[(totalJobsCreated + j - 1) % jobTemplates.length];

                JobCategory category = jobCategoryRepository.findByCategoryName(template[1])
                        .orElseThrow(() -> new RuntimeException("Category not found: " + template[1]));

                Job job = new Job();
                job.setEmployerId(employer.getId());
                job.setTitle(template[0] + " - " + companyName);
                job.setCategoryId(category.getId());
                job.setDescription(template[5]);
                job.setRequirements(template[6]);
                job.setEmploymentType(template[2]);
                job.setWorkMode(template[3]);
                job.setLocation(locations[(i + j - 2) % locations.length]);
                job.setFieldOfStudy(template[4]);

                BigDecimal minSalary;
                BigDecimal maxSalary;

                if ("internship".equals(template[2]) || "part-time".equals(template[2])) {
                    minSalary = BigDecimal.valueOf(24 + (j % 6));
                    maxSalary = BigDecimal.valueOf(32 + (j % 8));
                } else {
                    minSalary = BigDecimal.valueOf(54000 + ((i + j) % 10) * 3000L);
                    maxSalary = minSalary.add(BigDecimal.valueOf(15000 + (j % 5) * 2000L));
                }

                job.setSalaryMin(minSalary);
                job.setSalaryMax(maxSalary);
                job.setTargetStudentType(template[7]);
                job.setDeadline(LocalDateTime.now().plusDays(30 + ((i + j) % 60)));

                if ((i + j) % 11 == 0) {
                    job.setStatus("rejected");
                } else if ((i + j) % 5 == 0) {
                    job.setStatus("pending");
                } else {
                    job.setStatus("approved");
                }

                jobRepository.save(job);
            }

            totalJobsCreated += jobsForThisEmployer;
        }
    }
}