package com.talktranslate.service;

import com.talktranslate.exception.BadCredentialsException;
import com.talktranslate.model.User;
import com.talktranslate.model.dto.AuthResponse;
import com.talktranslate.model.dto.LoginRequest;
import com.talktranslate.model.dto.SignUpRequest;
import com.talktranslate.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private AuthService authService;

    private User existingUser;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        existingUser = new User("usr_999", "carlos_garcia", "carlos@example.com", "hashed_secret_pw", "Carlos García");
        existingUser.setPreferredLanguage("es");

        sampleUser = User.builder()
                .id("usr_456")
                .username("elena_rostova")
                .email("elena@example.com")
                .fullName("Elena Rostova")
                .preferredLanguage("ru")
                .build();
    }

    @Test
    void shouldRegisterNewUserSuccessfully() {
        SignUpRequest request = new SignUpRequest("rahul_sharma", "rahul@example.com", "SecureP@ss123", "Rahul Sharma");
        User savedUser = new User("usr_123", request.getUsername(), request.getEmail(), "encoded_pass",
                request.getFullName());

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtTokenService.generateToken(any(User.class))).thenReturn("jwt_mock_token_abc123");

        AuthResponse response = authService.signUp(request);

        assertAll("Validate Sign Up AuthResponse",
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.getToken()).isEqualTo("jwt_mock_token_abc123"),
                () -> assertThat(response.getUserId()).isEqualTo("usr_123"),
                () -> assertThat(response.getUsername()).isEqualTo("rahul_sharma"),
                () -> assertThat(response.getEmail()).isEqualTo("rahul@example.com"),
                () -> assertThat(response.getFullName()).isEqualTo("Rahul Sharma"),
                () -> assertThat(response.getPreferredLanguage()).isEqualTo("en"),
                () -> assertThat(response.getMessage()).isEqualTo("User registered successfully"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getUsername()).isEqualTo("rahul_sharma");
        assertThat(capturedUser.getEmail()).isEqualTo("rahul@example.com");
        assertThat(capturedUser.getPasswordHash()).isEqualTo("encoded_pass");
        assertThat(capturedUser.getFullName()).isEqualTo("Rahul Sharma");
        assertThat(capturedUser.getPreferredLanguage()).isEqualTo("en");

        verify(passwordEncoder, times(1)).encode("SecureP@ss123");
        verify(jwtTokenService, times(1)).generateToken(savedUser);
    }

    @Test
    void shouldTrimWhitespaceFromFieldsDuringSignUp() {
        SignUpRequest request = new SignUpRequest("  rahul_sharma  ", "  rahul@example.com  ", "SecureP@ss123",
                "  Rahul Sharma  ");
        User savedUser = new User("usr_123", "rahul_sharma", "rahul@example.com", "encoded_pass", "Rahul Sharma");

        when(userRepository.existsByUsername("rahul_sharma")).thenReturn(false);
        when(userRepository.existsByEmail("rahul@example.com")).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtTokenService.generateToken(any(User.class))).thenReturn("jwt_token_123");

        AuthResponse response = authService.signUp(request);

        assertThat(response.getUsername()).isEqualTo("rahul_sharma");
        assertThat(response.getEmail()).isEqualTo("rahul@example.com");
        assertThat(response.getFullName()).isEqualTo("Rahul Sharma");

        verify(userRepository).existsByUsername("rahul_sharma");
        verify(userRepository).existsByEmail("rahul@example.com");
    }

    @Test
    void shouldThrowExceptionWhenSignUpRequestIsNull() {
        assertThatThrownBy(() -> authService.signUp(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Registration request cannot be null");

        verifyNoInteractions(userRepository, passwordEncoder, jwtTokenService);
    }

    @ParameterizedTest(name = "Username rejected when invalid: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = { " ", "   ", "\t", "\n" })
    void shouldRejectNullOrBlankUsername(String invalidUsername) {
        SignUpRequest request = new SignUpRequest(invalidUsername, "valid@example.com", "StrongP@ss123", "John Doe");

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username is required and cannot be blank");

        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest(name = "Email rejected when invalid: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = { " ", "   ", "\t", "\n" })
    void shouldRejectNullOrBlankEmail(String invalidEmail) {
        SignUpRequest request = new SignUpRequest("valid_user", invalidEmail, "StrongP@ss123", "John Doe");

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is required and cannot be blank");

        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest(name = "Password rejected when invalid: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = { " ", "   ", "\t", "\n" })
    void shouldRejectNullOrBlankPassword(String invalidPassword) {
        SignUpRequest request = new SignUpRequest("valid_user", "valid@example.com", invalidPassword, "John Doe");

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password is required and cannot be blank");

        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest(name = "Full name rejected when invalid: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = { " ", "   ", "\t", "\n" })
    void shouldRejectNullOrBlankFullName(String invalidFullName) {
        SignUpRequest request = new SignUpRequest("valid_user", "valid@example.com", "StrongP@ss123", invalidFullName);

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Full Name is required and cannot be blank");

        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest(name = "Invalid username format: [{0}]")
    @ValueSource(strings = { "ab", "a", "user name", "user@name", "user#123", "user!name", "user$$", "user.name" })
    void shouldRejectInvalidUsernameFormats(String invalidUsername) {
        SignUpRequest request = new SignUpRequest(invalidUsername, "user@test.com", "SecureP@ss1", "Test User");

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid username format. Must be 3-30 alphanumeric characters, underscores, or hyphens");
    }

    @Test
    void shouldAcceptBoundaryLengthUsernamesLowerBound() {
        String lowerBoundaryUser = "usr";
        when(userRepository.existsByUsername(lowerBoundaryUser)).thenReturn(false);
        when(userRepository.existsByEmail("min@test.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_secret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenService.generateToken(any())).thenReturn("token_boundary");

        AuthResponse resp = authService
                .signUp(new SignUpRequest(lowerBoundaryUser, "min@test.com", "Pass1234!", "Min User"));
        assertThat(resp.getUsername()).isEqualTo(lowerBoundaryUser);
    }

    @Test
    void shouldAcceptBoundaryLengthUsernamesUpperBound() {
        String maxBoundaryUser = "a".repeat(30);
        when(userRepository.existsByUsername(maxBoundaryUser)).thenReturn(false);
        when(userRepository.existsByEmail("max@test.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_secret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenService.generateToken(any())).thenReturn("token_boundary_max");

        AuthResponse resp = authService
                .signUp(new SignUpRequest(maxBoundaryUser, "max@test.com", "Pass1234!", "Max User"));
        assertThat(resp.getUsername()).isEqualTo(maxBoundaryUser);
    }

    @Test
    void shouldRejectOverBoundaryUsername() {
        String overBoundaryUser = "a".repeat(31);
        SignUpRequest request = new SignUpRequest(overBoundaryUser, "over@test.com", "SecureP@ss1", "Over User");

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username exceeds maximum length of 30 characters");
    }

    @ParameterizedTest(name = "Invalid email format: [{0}]")
    @ValueSource(strings = {
            "plainaddress",
            "@missingusername.com",
            "username@.com",
            "user space@domain.com",
            "user..name@domain.com",
            "user@domain..com",
            "user@domain.c"
    })
    void shouldRejectMalformedEmailFormats(String malformedEmail) {
        SignUpRequest request = new SignUpRequest("valid_user", malformedEmail, "SecureP@ss1", "Test User");

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email address format");
    }

    @Test
    void shouldPreventDuplicateUsername() {
        SignUpRequest request = new SignUpRequest("existing_user", "unique@test.com", "SecureP@ss1", "User One");
        when(userRepository.existsByUsername("existing_user")).thenReturn(true);

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Username 'existing_user' is already taken");
    }

    @Test
    void shouldPreventDuplicateEmail() {
        SignUpRequest request = new SignUpRequest("new_user", "taken@test.com", "SecureP@ss1", "User Two");
        when(userRepository.existsByUsername("new_user")).thenReturn(false);
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Email 'taken@test.com' is already registered");
    }

    @ParameterizedTest(name = "Rejected weak password: [{0}]")
    @ValueSource(strings = { "123", "short", "nouppercase123!", "NOLOWERCASE123!", "NoSpecialCharacter123",
            "NoDigit!@#$%" })
    void shouldRejectWeakPasswords(String weakPassword) {
        SignUpRequest request = new SignUpRequest("valid_user", "user@test.com", weakPassword, "Test User");

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Password must be at least 8 characters long and contain uppercase, lowercase, digit, and special symbol");
    }

    @Test
    void shouldAcceptValidBoundaryPasswordLowerBound() {
        String exact8CharPassword = "Valid12!";
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_pwd");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenService.generateToken(any())).thenReturn("token_valid");

        AuthResponse resp = authService
                .signUp(new SignUpRequest("valid_user", "min@test.com", exact8CharPassword, "Valid User"));
        assertThat(resp).isNotNull();
    }

    @Test
    void shouldAcceptValidBoundaryPasswordUpperBound() {
        String exact128CharPassword = "A1!" + "a".repeat(125);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_pwd");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenService.generateToken(any())).thenReturn("token_valid");

        AuthResponse resp = authService
                .signUp(new SignUpRequest("valid_user", "max@test.com", exact128CharPassword, "Valid User"));
        assertThat(resp).isNotNull();
    }

    @Test
    void shouldRejectPasswordExceedingMaximumBoundary() {
        String overlongPassword = "A1!" + "a".repeat(126);
        SignUpRequest request = new SignUpRequest("dos_user", "dos@test.com", overlongPassword, "DoS User");

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password exceeds maximum allowed length of 128 characters");
    }

    @ParameterizedTest(name = "Valid international Unicode full name: [{0}]")
    @ValueSource(strings = {
            "Rahul Sharma",
            "राहुल शर्मा",
            "田中 太郎",
            "José Álvarez",
            "Éléonore François",
            "Александр Пушкин",
            "محمد عبد الله"
    })
    void shouldAcceptUnicodeFullNames(String unicodeName) {
        SignUpRequest request = new SignUpRequest("polyglot_user", "polyglot@test.com", "SecureP@ss123", unicodeName);

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_pw");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenService.generateToken(any())).thenReturn("token_valid");

        AuthResponse response = authService.signUp(request);
        assertThat(response.getFullName()).isEqualTo(unicodeName);
    }

    @Test
    void shouldAcceptValidBoundaryFullNameUpperBound() {
        String exact100Name = "A".repeat(100);
        SignUpRequest request = new SignUpRequest("valid_user", "user@test.com", "SecureP@ss123", exact100Name);

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_pw");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenService.generateToken(any())).thenReturn("token_valid");

        AuthResponse response = authService.signUp(request);
        assertThat(response.getFullName()).isEqualTo(exact100Name);
    }

    @Test
    void shouldRejectOverlongFullName() {
        String overlongName = "A".repeat(101);
        SignUpRequest request = new SignUpRequest("valid_user", "user@test.com", "SecureP@ss123", overlongName);

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Full Name exceeds maximum length of 100 characters");
    }

    @ParameterizedTest(name = "Forbidden HTML / XSS name: [{0}]")
    @ValueSource(strings = {
            "<script>alert(1)</script>",
            "John <Doe>",
            "Jane Doe>",
            "<Jane Doe",
            "Test script injection",
            "Malicious SCRIPT User"
    })
    void shouldRejectForbiddenHtmlAndScriptInFullName(String forbiddenName) {
        SignUpRequest request = new SignUpRequest("valid_user", "user@test.com", "SecureP@ss123", forbiddenName);

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Full Name contains invalid or forbidden characters");

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldLoginSuccessfullyWithUsername() {
        LoginRequest request = new LoginRequest("carlos_garcia", "CorrectP@ssword1");

        when(userRepository.findByUsernameOrEmail("carlos_garcia", "carlos_garcia"))
                .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("CorrectP@ssword1", "hashed_secret_pw")).thenReturn(true);
        when(jwtTokenService.generateToken(existingUser)).thenReturn("jwt_user_token_999");

        AuthResponse response = authService.login(request);

        assertAll("Validate Login AuthResponse",
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.getToken()).isEqualTo("jwt_user_token_999"),
                () -> assertThat(response.getUserId()).isEqualTo("usr_999"),
                () -> assertThat(response.getUsername()).isEqualTo("carlos_garcia"),
                () -> assertThat(response.getEmail()).isEqualTo("carlos@example.com"),
                () -> assertThat(response.getFullName()).isEqualTo("Carlos García"),
                () -> assertThat(response.getPreferredLanguage()).isEqualTo("es"),
                () -> assertThat(response.getMessage()).isEqualTo("Login successful"));

        verify(userRepository, times(1)).findByUsernameOrEmail("carlos_garcia", "carlos_garcia");
        verify(passwordEncoder, times(1)).matches("CorrectP@ssword1", "hashed_secret_pw");
        verify(jwtTokenService, times(1)).generateToken(existingUser);
    }

    @Test
    void shouldLoginSuccessfullyWithEmail() {
        LoginRequest request = new LoginRequest("carlos@example.com", "CorrectP@ssword1");

        when(userRepository.findByUsernameOrEmail("carlos@example.com", "carlos@example.com"))
                .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("CorrectP@ssword1", "hashed_secret_pw")).thenReturn(true);
        when(jwtTokenService.generateToken(existingUser)).thenReturn("jwt_email_token_999");

        AuthResponse response = authService.login(request);

        assertAll("Validate Login with Email AuthResponse",
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.getToken()).isEqualTo("jwt_email_token_999"),
                () -> assertThat(response.getUserId()).isEqualTo("usr_999"),
                () -> assertThat(response.getUsername()).isEqualTo("carlos_garcia"),
                () -> assertThat(response.getEmail()).isEqualTo("carlos@example.com"),
                () -> assertThat(response.getFullName()).isEqualTo("Carlos García"),
                () -> assertThat(response.getPreferredLanguage()).isEqualTo("es"),
                () -> assertThat(response.getMessage()).isEqualTo("Login successful"));
    }

    @Test
    void shouldTrimWhitespaceFromIdentifierDuringLogin() {
        LoginRequest request = new LoginRequest("  carlos_garcia  ", "CorrectP@ssword1");

        when(userRepository.findByUsernameOrEmail("carlos_garcia", "carlos_garcia"))
                .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("CorrectP@ssword1", "hashed_secret_pw")).thenReturn(true);
        when(jwtTokenService.generateToken(existingUser)).thenReturn("jwt_token");

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        verify(userRepository).findByUsernameOrEmail("carlos_garcia", "carlos_garcia");
    }

    @Test
    void shouldThrowExceptionWhenLoginRequestIsNull() {
        assertThatThrownBy(() -> authService.login(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Login request cannot be null");

        verifyNoInteractions(userRepository, passwordEncoder, jwtTokenService);
    }

    @ParameterizedTest(name = "Identifier rejected when invalid: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = { " ", "   ", "\t", "\n" })
    void shouldRejectNullOrBlankUsernameOrEmail(String invalidIdentifier) {
        LoginRequest request = new LoginRequest(invalidIdentifier, "ValidPass123!");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username or Email is required");

        verifyNoInteractions(userRepository, passwordEncoder, jwtTokenService);
    }

    @ParameterizedTest(name = "Password rejected when invalid: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = { " ", "   ", "\t", "\n" })
    void shouldRejectNullOrBlankPasswordOnLogin(String invalidPassword) {
        LoginRequest request = new LoginRequest("valid_user", invalidPassword);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password is required");

        verifyNoInteractions(userRepository, passwordEncoder, jwtTokenService);
    }

    @Test
    void shouldFailLoginForNonExistentUser() {
        LoginRequest request = new LoginRequest("ghost_user", "AnyPassword123!");
        when(userRepository.findByUsernameOrEmail("ghost_user", "ghost_user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid username/email or password");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenService, never()).generateToken(any());
    }

    @Test
    void shouldFailLoginForIncorrectPassword() {
        User user = new User("usr_100", "test_user", "test@test.com", "correct_encoded_hash", "Test User");
        LoginRequest request = new LoginRequest("test_user", "WrongP@ssword999");

        when(userRepository.findByUsernameOrEmail("test_user", "test_user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongP@ssword999", "correct_encoded_hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid username/email or password");

        verify(jwtTokenService, never()).generateToken(any());
    }

    @Test
    void shouldReturnCurrentUserWhenTokenIsValid() {
        String token = "valid_jwt_token_sample";

        when(jwtTokenService.validateToken(token)).thenReturn(true);
        when(jwtTokenService.extractUserId(token)).thenReturn("usr_456");
        when(userRepository.findById("usr_456")).thenReturn(Optional.of(sampleUser));

        User result = authService.getCurrentUser(token);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("usr_456");
        assertThat(result.getUsername()).isEqualTo("elena_rostova");
        assertThat(result.getEmail()).isEqualTo("elena@example.com");
        assertThat(result.getFullName()).isEqualTo("Elena Rostova");
        assertThat(result.getPreferredLanguage()).isEqualTo("ru");

        verify(jwtTokenService, times(1)).validateToken(token);
        verify(jwtTokenService, times(1)).extractUserId(token);
        verify(userRepository, times(1)).findById("usr_456");
    }

    @Test
    void shouldThrowExceptionWhenTokenIsInvalidOrExpired() {
        String invalidToken = "invalid_or_expired_jwt";

        when(jwtTokenService.validateToken(invalidToken)).thenReturn(false);

        assertThatThrownBy(() -> authService.getCurrentUser(invalidToken))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid or expired authentication token");

        verify(jwtTokenService, never()).extractUserId(anyString());
        verify(userRepository, never()).findById(anyString());
    }

    @Test
    void shouldThrowExceptionWhenTokenIsNull() {
        when(jwtTokenService.validateToken(null)).thenReturn(false);

        assertThatThrownBy(() -> authService.getCurrentUser(null))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid or expired authentication token");

        verify(jwtTokenService, never()).extractUserId(anyString());
        verify(userRepository, never()).findById(anyString());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundInRepository() {
        String token = "valid_token_ghost_user";

        when(jwtTokenService.validateToken(token)).thenReturn(true);
        when(jwtTokenService.extractUserId(token)).thenReturn("usr_non_existent");
        when(userRepository.findById("usr_non_existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getCurrentUser(token))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("User not found");

        verify(jwtTokenService, times(1)).validateToken(token);
        verify(jwtTokenService, times(1)).extractUserId(token);
        verify(userRepository, times(1)).findById("usr_non_existent");
    }
}
