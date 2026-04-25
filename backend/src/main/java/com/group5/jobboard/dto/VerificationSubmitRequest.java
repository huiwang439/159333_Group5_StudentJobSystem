package com.group5.jobboard.dto;

import jakarta.validation.constraints.NotBlank;

public class VerificationSubmitRequest {

    @NotBlank(message = "businessLicenseUrl is required")
    private String businessLicenseUrl;

    private String supportingDocumentUrl;

    public String getBusinessLicenseUrl() {
        return businessLicenseUrl;
    }

    public void setBusinessLicenseUrl(String businessLicenseUrl) {
        this.businessLicenseUrl = businessLicenseUrl;
    }

    public String getSupportingDocumentUrl() {
        return supportingDocumentUrl;
    }

    public void setSupportingDocumentUrl(String supportingDocumentUrl) {
        this.supportingDocumentUrl = supportingDocumentUrl;
    }
}