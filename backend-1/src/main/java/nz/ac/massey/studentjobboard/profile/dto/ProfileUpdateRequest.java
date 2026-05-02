package nz.ac.massey.studentjobboard.profile.dto;

public record ProfileUpdateRequest(
    Long studentId,
    String fullName,
    String email,
    String studentNumber,
    String phone,
    String major,
    String location,
    String bio
) {}
