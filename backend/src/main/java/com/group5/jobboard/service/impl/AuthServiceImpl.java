package com.group5.jobboard.service.impl;

import com.group5.jobboard.config.JwtUtil;
import com.group5.jobboard.dto.LoginRequest;
import com.group5.jobboard.dto.RegisterRequest;
import com.group5.jobboard.entity.AnalyticsLog;
import com.group5.jobboard.entity.User;
import com.group5.jobboard.repository.AnalyticsLogRepository;
import com.group5.jobboard.repository.UserRepository;
import com.group5.jobboard.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AnalyticsLogRepository analyticsLogRepository;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           AnalyticsLogRepository analyticsLogRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.analyticsLogRepository = analyticsLogRepository;
    }

    @Override
    public Map<String, Object> register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (!"student".equals(request.getRole()) && !"employer".equals(request.getRole())) {
            throw new RuntimeException("Only student and employer can register");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());
        user.setAccountStatus("active");

        userRepository.save(user);

        log(user.getId(), "REGISTER", "USER", user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("role", user.getRole());
        result.put("accountStatus", user.getAccountStatus());

        return result;
    }

    @Override
    public Map<String, Object> login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (!"active".equals(user.getAccountStatus())) {
            throw new RuntimeException("Your account has been disabled or banned");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getRole(), user.getEmail());

        log(user.getId(), "LOGIN", "USER", user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("role", user.getRole());
        result.put("fullName", user.getFullName());
        result.put("accountStatus", user.getAccountStatus());

        return result;
    }

    @Override
    public Map<String, Object> logout(Long userId) {
        log(userId, "LOGOUT", "USER", userId);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("logout", true);
        result.put("message", "logout success, please remove token on frontend");

        return result;
    }

    @Override
    public Map<String, Object> getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("fullName", user.getFullName());
        result.put("email", user.getEmail());
        result.put("role", user.getRole());
        result.put("phone", user.getPhone());
        result.put("accountStatus", user.getAccountStatus());

        return result;
    }

    private void log(Long userId, String actionType, String targetType, Long targetId) {
        AnalyticsLog log = new AnalyticsLog();
        log.setUserId(userId);
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        analyticsLogRepository.save(log);
    }
}