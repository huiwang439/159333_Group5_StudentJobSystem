package com.group5.jobboard.dto;

import jakarta.validation.constraints.NotBlank;

public class UserStatusUpdateRequest {

    @NotBlank(message = "status cannot be blank")
    private String status;

    public UserStatusUpdateRequest() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}