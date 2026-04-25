package com.group5.jobboard.dto;

import jakarta.validation.constraints.NotBlank;

public class VerificationReviewRequest {

    @NotBlank(message = "reviewStatus is required")
    private String reviewStatus;

    private String reviewNote;

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }
}