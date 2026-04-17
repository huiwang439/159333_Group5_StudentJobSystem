package nz.ac.massey.studentjobboard.application.dto;

public record ApplyRequest(
    Long jobId,
    Long studentId,
    String resumeName,
    String coverLetterNote
) {}
