package nz.ac.massey.studentjobboard.application.dto;

public record ApplicationSummaryResponse(
    long totalApplied,
    long pendingCount
) {}
