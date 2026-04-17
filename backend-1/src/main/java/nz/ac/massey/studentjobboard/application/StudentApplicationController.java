package nz.ac.massey.studentjobboard.application;

import java.util.List;
import nz.ac.massey.studentjobboard.application.dto.ApplicationDetailResponse;
import nz.ac.massey.studentjobboard.application.dto.ApplicationItemResponse;
import nz.ac.massey.studentjobboard.application.dto.ApplicationSummaryResponse;
import nz.ac.massey.studentjobboard.application.dto.ApplicationTimelineItemResponse;
import nz.ac.massey.studentjobboard.application.dto.ApplyRequest;
import nz.ac.massey.studentjobboard.job.Job;
import nz.ac.massey.studentjobboard.job.JobRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/student/applications")
@CrossOrigin(origins = "*")
public class StudentApplicationController {

    private final StudentApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository historyRepository;
    private final JobRepository jobRepository;

    public StudentApplicationController(
        StudentApplicationRepository applicationRepository,
        ApplicationStatusHistoryRepository historyRepository,
        JobRepository jobRepository
    ) {
        this.applicationRepository = applicationRepository;
        this.historyRepository = historyRepository;
        this.jobRepository = jobRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationItemResponse apply(@RequestBody ApplyRequest request) {
        if (request.jobId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "jobId is required");
        }
        Long studentId = request.studentId() == null ? 1L : request.studentId();
        Job job = jobRepository.findById(request.jobId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        StudentApplication application = new StudentApplication();
        application.setStudentId(studentId);
        application.setJob(job);
        application.setStatus("Submitted");
        application.setResumeName(
            request.resumeName() == null || request.resumeName().isBlank() ? "Default Resume.pdf" : request.resumeName().trim()
        );
        application.setCoverLetterNote(
            request.coverLetterNote() == null || request.coverLetterNote().isBlank() ? "No extra note." : request.coverLetterNote().trim()
        );
        application = applicationRepository.save(application);

        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplication(application);
        history.setOldStatus("N/A");
        history.setNewStatus("Submitted");
        history.setNote("Application submitted successfully.");
        historyRepository.save(history);

        return mapApplication(application);
    }

    @GetMapping
    public List<ApplicationItemResponse> list(@RequestParam(defaultValue = "1") Long studentId) {
        return applicationRepository.findByStudentIdOrderByAppliedAtDesc(studentId).stream()
            .map(this::mapApplication)
            .toList();
    }

    @GetMapping("/summary")
    public ApplicationSummaryResponse summary(@RequestParam(defaultValue = "1") Long studentId) {
        long totalApplied = applicationRepository.countByStudentId(studentId);
        long pendingCount = applicationRepository.countByStudentIdAndStatusIn(studentId, List.of("Submitted", "In Review"));
        return new ApplicationSummaryResponse(totalApplied, pendingCount);
    }

    @GetMapping("/{applicationId}")
    public ApplicationDetailResponse detail(
        @PathVariable Long applicationId,
        @RequestParam(defaultValue = "1") Long studentId
    ) {
        StudentApplication application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
        if (!application.getStudentId().equals(studentId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
        }
        List<ApplicationTimelineItemResponse> timeline = historyRepository.findByApplicationIdOrderByChangedAtAsc(applicationId)
            .stream()
            .map(item -> new ApplicationTimelineItemResponse(
                item.getOldStatus(),
                item.getNewStatus(),
                item.getNote(),
                item.getChangedAt()
            ))
            .toList();
        return new ApplicationDetailResponse(mapApplication(application), timeline);
    }

    private ApplicationItemResponse mapApplication(StudentApplication application) {
        Job job = application.getJob();
        return new ApplicationItemResponse(
            application.getId(),
            job.getId(),
            job.getTitle(),
            job.getCompanyName(),
            job.getLocation(),
            job.getJobType(),
            application.getStatus(),
            application.getAppliedAt(),
            application.getResumeName()
        );
    }
}
