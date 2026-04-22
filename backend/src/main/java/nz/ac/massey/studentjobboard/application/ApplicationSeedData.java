package nz.ac.massey.studentjobboard.application;

import java.time.LocalDateTime;
import java.util.List;
import nz.ac.massey.studentjobboard.job.Job;
import nz.ac.massey.studentjobboard.job.JobRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ApplicationSeedData implements CommandLineRunner {

    private final StudentApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository historyRepository;
    private final JobRepository jobRepository;

    public ApplicationSeedData(
        StudentApplicationRepository applicationRepository,
        ApplicationStatusHistoryRepository historyRepository,
        JobRepository jobRepository
    ) {
        this.applicationRepository = applicationRepository;
        this.historyRepository = historyRepository;
        this.jobRepository = jobRepository;
    }

    @Override
    public void run(String... args) {
        if (applicationRepository.countByStudentId(1L) > 0) {
            return;
        }
        List<Job> jobs = jobRepository.findAllByOrderByIdDesc(org.springframework.data.domain.PageRequest.of(0, 3));
        if (jobs.isEmpty()) {
            return;
        }

        StudentApplication first = createApplication(jobs.get(0), "In Review", LocalDateTime.now().minusDays(3), "Resume_Alex.pdf");
        StudentApplication second = createApplication(jobs.size() > 1 ? jobs.get(1) : jobs.get(0), "Submitted", LocalDateTime.now().minusDays(2), "Resume_Alex.pdf");
        StudentApplication third = createApplication(jobs.size() > 2 ? jobs.get(2) : jobs.get(0), "Shortlisted", LocalDateTime.now().minusDays(1), "Resume_Alex.pdf");

        seedHistory(first, List.of(
            history("N/A", "Submitted", "Application submitted."),
            history("Submitted", "In Review", "Employer is reviewing your profile.")
        ));
        seedHistory(second, List.of(
            history("N/A", "Submitted", "Application submitted.")
        ));
        seedHistory(third, List.of(
            history("N/A", "Submitted", "Application submitted."),
            history("Submitted", "In Review", "Profile moved to screening stage."),
            history("In Review", "Shortlisted", "You were shortlisted for next step.")
        ));
    }

    private StudentApplication createApplication(Job job, String status, LocalDateTime appliedAt, String resumeName) {
        StudentApplication app = new StudentApplication();
        app.setStudentId(1L);
        app.setJob(job);
        app.setStatus(status);
        app.setAppliedAt(appliedAt);
        app.setResumeName(resumeName);
        app.setCoverLetterNote("Motivated student ready for practical experience.");
        return applicationRepository.save(app);
    }

    private ApplicationStatusHistory history(String oldStatus, String newStatus, String note) {
        ApplicationStatusHistory item = new ApplicationStatusHistory();
        item.setOldStatus(oldStatus);
        item.setNewStatus(newStatus);
        item.setNote(note);
        item.setChangedAt(LocalDateTime.now());
        return item;
    }

    private void seedHistory(StudentApplication app, List<ApplicationStatusHistory> historyItems) {
        for (ApplicationStatusHistory item : historyItems) {
            item.setApplication(app);
            historyRepository.save(item);
        }
    }
}
