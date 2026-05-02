package com.group5.jobboard.config;

import com.group5.jobboard.entity.*;
import com.group5.jobboard.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
        System.out.println("===== TestDataInitializer started =====");

        createAdmin();
        createCategories();
        createStudents(30);
        createEmployersAndJobs(10, 5);

        System.out.println("===== TestDataInitializer finished =====");
    }

    private void createAdmin() {
        if (userRepository.findByEmail("admin@test.com").isPresent()) {
            System.out.println("Admin already exists.");
            return;
        }

        User admin = new User();
        admin.setFullName("Admin User");
        admin.setEmail("admin@test.com");
        admin.setPasswordHash(passwordEncoder.encode("A123456"));
        admin.setRole("admin");
        admin.setPhone("9999999999");
        admin.setAccountStatus("active");

        userRepository.save(admin);

        System.out.println("Admin created: admin@test.com / A123456");
    }

    private void createCategories() {
        createCategory("Software Engineering", "Software development and backend engineering");
        createCategory("Data Analysis", "Data analytics and business intelligence");
        createCategory("Marketing", "Marketing and digital operation");
        createCategory("Design", "UI, UX and creative design");
        createCategory("Business", "Business operation and management");

        System.out.println("Categories checked.");
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
        String[] majors = {
                "Computer Science",
                "Data Science",
                "Business",
                "Marketing",
                "Design"
        };

        String[] degrees = {
                "Bachelor",
                "Master"
        };

        String[] locations = {
                "Auckland",
                "Wellington",
                "Christchurch",
                "Hamilton"
        };

        for (int i = 1; i <= count; i++) {
            String email = "student" + i + "@test.com";

            if (userRepository.findByEmail(email).isPresent()) {
                continue;
            }

            User user = new User();
            user.setFullName("Student " + i);
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode("123456A"));
            user.setRole("student");
            user.setPhone("0210000" + i);
            user.setAccountStatus(i % 10 == 0 ? "banned" : "active");

            userRepository.save(user);

            StudentProfile profile = new StudentProfile();
            profile.setUserId(user.getId());
            profile.setUniversity("Massey University");
            profile.setMajor(majors[(i - 1) % majors.length]);
            profile.setDegreeLevel(degrees[(i - 1) % degrees.length]);
            profile.setGraduationYear(2026);
            profile.setSkills("Java, SQL, Communication");
            profile.setBio("Test student profile " + i);
            profile.setPreferredLocation(locations[(i - 1) % locations.length]);
            profile.setPreferredJobType(i % 2 == 0 ? "internship" : "full-time");

            studentProfileRepository.save(profile);
        }

        System.out.println("Students checked.");
    }

    private void createEmployersAndJobs(int employerCount, int jobsPerEmployer) {
        List<JobCategory> categories = jobCategoryRepository.findAll();

        if (categories.isEmpty()) {
            throw new RuntimeException("No job categories found. Please create categories first.");
        }

        String[] industries = {
                "Technology",
                "Finance",
                "Education",
                "Design",
                "Marketing"
        };

        String[] locations = {
                "Auckland",
                "Wellington",
                "Christchurch",
                "Hamilton"
        };

        String[] employmentTypes = {
                "internship",
                "full-time",
                "part-time"
        };

        String[] fields = {
                "Computer Science",
                "Data Science",
                "Business",
                "Marketing",
                "Design"
        };

        for (int i = 1; i <= employerCount; i++) {
            String email = "employer" + i + "@test.com";

            User employer;

            if (userRepository.findByEmail(email).isPresent()) {
                employer = userRepository.findByEmail(email).get();
            } else {
                employer = new User();
                employer.setFullName("Employer HR " + i);
                employer.setEmail(email);
                employer.setPasswordHash(passwordEncoder.encode("123456E"));
                employer.setRole("employer");
                employer.setPhone("0330000" + i);
                employer.setAccountStatus(i % 9 == 0 ? "disabled" : "active");

                userRepository.save(employer);
            }

            if (employerProfileRepository.findByUserId(employer.getId()).isEmpty()) {
                EmployerProfile profile = new EmployerProfile();
                profile.setUserId(employer.getId());
                profile.setCompanyName("Company " + i);
                profile.setIndustry(industries[(i - 1) % industries.length]);
                profile.setCompanySize(i % 2 == 0 ? "50-100" : "20-50");
                profile.setWebsite("https://company" + i + ".com");
                profile.setLocation(locations[(i - 1) % locations.length]);
                profile.setCompanyDescription("Test company " + i + " description");
                profile.setContactPerson("HR " + i);
                profile.setContactEmail(email);
                profile.setVerificationStatus(i % 3 == 0 ? "pending" : "approved");

                employerProfileRepository.save(profile);
            }

            long existingJobCount = jobRepository.findByEmployerId(employer.getId()).size();

            if (existingJobCount >= jobsPerEmployer) {
                continue;
            }

            for (int j = 1; j <= jobsPerEmployer; j++) {
                JobCategory category = categories.get((j - 1) % categories.size());

                Job job = new Job();
                job.setEmployerId(employer.getId());
                job.setCategoryId(category.getId());

                String field = fields[(j - 1) % fields.length];
                String employmentType = employmentTypes[(j - 1) % employmentTypes.length];

                job.setTitle(field + " " + employmentType + " Role " + i + "-" + j);
                job.setDescription("This is a test job for " + field + ".");
                job.setRequirements("Basic knowledge of " + field + ", communication skills, and teamwork.");
                job.setEmploymentType(employmentType);
                job.setWorkMode(j % 2 == 0 ? "hybrid" : "onsite");
                job.setLocation(locations[(j - 1) % locations.length]);
                job.setFieldOfStudy(field);
                job.setSalaryMin(BigDecimal.valueOf(20 + j));
                job.setSalaryMax(BigDecimal.valueOf(30 + j));

                if (j % 5 == 0) {
                    job.setStatus("pending");
                } else if (j % 7 == 0) {
                    job.setStatus("rejected");
                } else {
                    job.setStatus("approved");
                }

                jobRepository.save(job);
            }
        }

        System.out.println("Employers and jobs checked.");
    }
}