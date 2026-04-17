package nz.ac.massey.studentjobboard.profile;

import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ProfileSeedData implements CommandLineRunner {

    private final StudentProfileRepository profileRepository;
    private final StudentDocumentRepository documentRepository;

    public ProfileSeedData(StudentProfileRepository profileRepository, StudentDocumentRepository documentRepository) {
        this.profileRepository = profileRepository;
        this.documentRepository = documentRepository;
    }

    @Override
    public void run(String... args) {
        StudentProfile profile = profileRepository.findByStudentId(1L).orElse(null);
        if (profile == null) {
            profile = new StudentProfile();
            profile.setStudentId(1L);
            profile.setFullName("Alex Liu");
            profile.setEmail("alex.liu@campus.edu");
            profile.setStudentNumber("20210004");
            profile.setPhone("13800138004");
            profile.setMajor("Software Engineering");
            profile.setLocation("Auckland");
            profile.setBio("I focus on web application development and enjoy building clean user interfaces.");
            profile.setUpdatedAt(LocalDateTime.now());
            profileRepository.save(profile);
        }

        if (documentRepository.findByStudentIdOrderByUploadedAtDesc(1L).isEmpty()) {
            StudentDocument resume = new StudentDocument();
            resume.setStudentId(1L);
            resume.setDocumentType("resume");
            resume.setFileName("Resume_Alex.pdf");
            resume.setFileUrl("/uploads/sample-resume.pdf");
            resume.setFileSize(256000L);
            resume.setDefaultResume(true);
            resume.setUploadedAt(LocalDateTime.now().minusDays(5));
            documentRepository.save(resume);

            StudentDocument portfolio = new StudentDocument();
            portfolio.setStudentId(1L);
            portfolio.setDocumentType("portfolio");
            portfolio.setFileName("Portfolio_Alex.pdf");
            portfolio.setFileUrl("/uploads/sample-portfolio.pdf");
            portfolio.setFileSize(1024000L);
            portfolio.setDefaultResume(false);
            portfolio.setUploadedAt(LocalDateTime.now().minusDays(3));
            documentRepository.save(portfolio);
        }
    }
}
