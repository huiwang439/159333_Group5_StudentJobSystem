package nz.ac.massey.studentjobboard.application.dto;

import java.time.LocalDateTime;

public record ApplicationItemResponse(
    Long applicationId,
    Long jobId,
    String jobTitle,
    String companyName,
    String location,
    String jobType,
    String status,
    LocalDateTime appliedAt,
    String resumeName
) {}
