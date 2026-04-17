package nz.ac.massey.studentjobboard.application.dto;

import java.time.LocalDateTime;

public record ApplicationTimelineItemResponse(
    String oldStatus,
    String newStatus,
    String note,
    LocalDateTime changedAt
) {}
