package com.group5.jobboard.service.impl;

import com.group5.jobboard.config.JwtUtil;
import com.group5.jobboard.dto.LoginRequest;
import com.group5.jobboard.dto.RegisterRequest;
import com.group5.jobboard.entity.User;
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

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Map<String, Object> register(RegisterRequest request) {

        System.out.println("register request: "
                + request.getFullName() + ", "
                + request.getEmail() + ", "
                + request.getPassword() + ", "
                + request.getRole() + ", "
                + request.getPhone());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (!request.getRole().equals("student")
                && !request.getRole().equals("employer")
                && !request.getRole().equals("admin")) {
            throw new RuntimeException("Invalid role");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());
        user.setAccountStatus("active");

        userRepository.save(user);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("role", user.getRole());

        return result;
    }

    @Override
    public Map<String, Object> login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getRole(), user.getEmail());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("role", user.getRole());
        result.put("fullName", user.getFullName());

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
        result.put("accountStatus", user.getAccountStatus());

        return result;
    }
}