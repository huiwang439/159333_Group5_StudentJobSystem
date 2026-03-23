package com.group5.jobboard.dto;

import jakarta.validation.constraints.NotNull;

public class ApplicationCreateRequest {

    @NotNull(message = "jobId cannot be null")
    private Long jobId;

    private String coverLetterText;

    public ApplicationCreateRequest() {
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getCoverLetterText() {
        return coverLetterText;
    }

    public void setCoverLetterText(String coverLetterText) {
        this.coverLetterText = coverLetterText;
    }
}