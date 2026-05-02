package nz.ac.massey.studentjobboard.application.dto;

import java.util.List;

public record ApplicationDetailResponse(
    ApplicationItemResponse application,
    List<ApplicationTimelineItemResponse> timeline
) {}
