package com.group5.jobboard.dto;

import jakarta.validation.constraints.NotBlank;

public class JobStatusUpdateRequest {

    @NotBlank(message = "status cannot be blank")
    private String status;

    private String reason;

    public JobStatusUpdateRequest() {
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}