package com.talktranslate.controller;

import com.talktranslate.exception.BadCredentialsException;
import com.talktranslate.model.User;
import com.talktranslate.model.dto.AuthResponse;
import com.talktranslate.model.dto.LoginRequest;
import com.talktranslate.model.dto.SignUpRequest;
import com.talktranslate.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication and User Profile REST Controller.
 * Provides endpoints for registration, authentication, and authenticated profile retrieval.
 * Exceptions are handled globally by {@link com.talktranslate.exception.GlobalExceptionHandler}.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * User Registration (Sign Up)
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUp(@RequestBody SignUpRequest request) {
        AuthResponse response = authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * User Login
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get Current Authenticated User Profile
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new BadCredentialsException("Missing Authorization header");
        }
        User user = authService.getCurrentUser(authHeader);
        return ResponseEntity.ok(user);
    }
}
