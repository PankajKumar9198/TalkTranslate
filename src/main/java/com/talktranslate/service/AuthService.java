package com.talktranslate.service;

import com.talktranslate.model.User;
import com.talktranslate.model.dto.AuthResponse;
import com.talktranslate.model.dto.LoginRequest;
import com.talktranslate.model.dto.SignUpRequest;
import com.talktranslate.repository.UserRepository;
import com.talktranslate.exception.BadCredentialsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

/**
 * Service handling authentication operations including registration, login, and token resolution.
 */
@Service
@Transactional(readOnly = true)
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    // Validation Regex Patterns
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,30}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PASSWORD_COMPLEXITY = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+=-]).{8,}$");
    private static final Pattern FORBIDDEN_HTML_CHARS = Pattern.compile("[<>]|script", Pattern.CASE_INSENSITIVE);

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * Signs up a new user with validation and password encryption.
     *
     * @param request the registration request
     * @return the authentication response with generated JWT token
     */
    @Transactional
    public AuthResponse signUp(SignUpRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Registration request cannot be null");
        }

        // 1. Required field validations
        String rawUsername = request.getUsername();
        if (rawUsername == null || rawUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required and cannot be blank");
        }
        final String username = rawUsername.trim();

        String rawEmail = request.getEmail();
        if (rawEmail == null || rawEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required and cannot be blank");
        }
        final String email = rawEmail.trim();

        String rawPassword = request.getPassword();
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required and cannot be blank");
        }
        final String password = rawPassword;

        String rawFullName = request.getFullName();
        if (rawFullName == null || rawFullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full Name is required and cannot be blank");
        }
        final String fullName = rawFullName.trim();

        // 2. Username format and boundary checks
        if (username.length() > 30) {
            throw new IllegalArgumentException("Username exceeds maximum length of 30 characters");
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("Invalid username format. Must be 3-30 alphanumeric characters, underscores, or hyphens");
        }
        if (userRepository.existsByUsername(username)) {
            logger.warn("Sign-up attempt failed: Username '{}' already exists", username);
            throw new IllegalStateException("Username '" + username + "' is already taken");
        }

        // 3. Email format and uniqueness
        if (!EMAIL_PATTERN.matcher(email).matches() || email.contains("..") || email.contains(" ")) {
            throw new IllegalArgumentException("Invalid email address format");
        }
        if (userRepository.existsByEmail(email)) {
            logger.warn("Sign-up attempt failed: Email '{}' already registered", email);
            throw new IllegalStateException("Email '" + email + "' is already registered");
        }

        // 4. Password boundaries & complexity
        if (password.length() > 128) {
            throw new IllegalArgumentException("Password exceeds maximum allowed length of 128 characters");
        }
        if (!PASSWORD_COMPLEXITY.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must be at least 8 characters long and contain uppercase, lowercase, digit, and special symbol");
        }

        // 5. Full Name boundaries & sanitization
        if (fullName.length() > 100) {
            throw new IllegalArgumentException("Full Name exceeds maximum length of 100 characters");
        }
        if (FORBIDDEN_HTML_CHARS.matcher(fullName).find()) {
            throw new IllegalArgumentException("Full Name contains invalid or forbidden characters");
        }

        // 6. Password Hashing & Entity Creation
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(encodedPassword);
        user.setFullName(fullName);
        user.setPreferredLanguage("en"); // Default initial language

        User savedUser = userRepository.save(user);
        logger.info("Successfully registered user with id: {}, username: {}", savedUser.getId(), savedUser.getUsername());

        // 7. Token Generation
        String token = jwtTokenService.generateToken(savedUser);

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getPreferredLanguage(),
                "User registered successfully"
        );
    }

    /**
     * Authenticates a user using username or email and password.
     *
     * @param request the login request containing credentials
     * @return the authentication response with JWT token
     */
    public AuthResponse login(LoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Login request cannot be null");
        }

        String rawIdentifier = request.getUsernameOrEmail();
        if (rawIdentifier == null || rawIdentifier.trim().isEmpty()) {
            throw new IllegalArgumentException("Username or Email is required");
        }
        final String identifier = rawIdentifier.trim();

        String password = request.getPassword();
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        // Lookup user by Username or Email
        User user = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> {
                    logger.warn("Authentication failed: No user found for identifier '{}'", identifier);
                    return new BadCredentialsException("Invalid username/email or password");
                });

        // Match password hash
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            logger.warn("Authentication failed: Incorrect password for user '{}'", user.getUsername());
            throw new BadCredentialsException("Invalid username/email or password");
        }

        logger.info("User '{}' authenticated successfully", user.getUsername());

        // Generate token
        String token = jwtTokenService.generateToken(user);

        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPreferredLanguage(),
                "Login successful"
        );
    }

    /**
     * Retrieves currently authenticated user from token.
     *
     * @param token the JWT bearer token
     * @return the authenticated User entity
     */
    public User getCurrentUser(String token) {
        if (!jwtTokenService.validateToken(token)) {
            logger.warn("Invalid or expired token provided to getCurrentUser");
            throw new BadCredentialsException("Invalid or expired authentication token");
        }
        final String userId = jwtTokenService.extractUserId(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("User lookup failed for token user ID: {}", userId);
                    return new BadCredentialsException("User not found");
                });
    }
}
