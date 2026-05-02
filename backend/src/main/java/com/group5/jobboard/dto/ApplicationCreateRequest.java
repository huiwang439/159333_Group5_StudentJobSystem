package com.group5.jobboard.dto;

import jakarta.validation.constraints.NotNull;

public class ApplicationCreateRequest {

    @NotNull(message = "jobId cannot be null")
    private Long jobId;

    private String coverLetterText;

    private Long resumeDocumentId;

    private Long portfolioDocumentId;

    public ApplicationCreateRequest() {}

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

    public Long getResumeDocumentId() {
        return resumeDocumentId;
    }

    public void setResumeDocumentId(Long resumeDocumentId) {
        this.resumeDocumentId = resumeDocumentId;
    }

    public Long getPortfolioDocumentId() {
        return portfolioDocumentId;
    }

    public void setPortfolioDocumentId(Long portfolioDocumentId) {
        this.portfolioDocumentId = portfolioDocumentId;
    }
}