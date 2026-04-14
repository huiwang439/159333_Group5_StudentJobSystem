package com.group5.jobboard.service;

import com.group5.jobboard.dto.LoginRequest;
import com.group5.jobboard.dto.RegisterRequest;

import java.util.Map;

public interface AuthService {

    Map<String, Object> register(RegisterRequest request);

    Map<String, Object> login(LoginRequest request);

    Map<String, Object> getCurrentUser(Long userId);

}