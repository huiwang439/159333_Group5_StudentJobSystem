package nz.ac.massey.studentjobboard.notification;

import java.time.LocalDateTime;
import java.util.List;
import nz.ac.massey.studentjobboard.application.StudentApplication;
import nz.ac.massey.studentjobboard.application.StudentApplicationRepository;
import nz.ac.massey.studentjobboard.job.Job;
import nz.ac.massey.studentjobboard.job.JobRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class NotificationSeedData implements CommandLineRunner {

    private final StudentNotificationRepository notificationRepository;
    private final JobRepository jobRepository;
    private final StudentApplicationRepository applicationRepository;

    public NotificationSeedData(
        StudentNotificationRepository notificationRepository,
        JobRepository jobRepository,
        StudentApplicationRepository applicationRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    @Override
    public void run(String... args) {
        if (!notificationRepository.findByStudentIdOrderByCreatedAtDesc(1L).isEmpty()) {
            return;
        }

        List<Job> latestJobs = jobRepository.findAllByOrderByIdDesc(PageRequest.of(0, 3));
        for (Job job : latestJobs) {
            StudentNotification item = new StudentNotification();
            item.setStudentId(1L);
            item.setType("job");
            item.setTitle("New job posted");
            item.setMessage(job.getTitle() + " at " + job.getCompanyName() + " matches your profile.");
            item.setRead(false);
            item.setCreatedAt(LocalDateTime.now().minusHours(6));
            notificationRepository.save(item);
        }

        List<StudentApplication> apps = applicationRepository.findByStudentIdOrderByAppliedAtDesc(1L);
        for (StudentApplication app : apps.stream().limit(3).toList()) {
            StudentNotification item = new StudentNotification();
            item.setStudentId(1L);
            item.setType("application");
            item.setTitle("Application status update");
            item.setMessage(app.getJob().getTitle() + " is now " + app.getStatus() + ".");
            item.setRead(false);
            item.setCreatedAt(LocalDateTime.now().minusHours(2));
            notificationRepository.save(item);
        }
    }
}
