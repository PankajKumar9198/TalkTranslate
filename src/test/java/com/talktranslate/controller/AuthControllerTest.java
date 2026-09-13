package com.talktranslate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talktranslate.exception.BadCredentialsException;
import com.talktranslate.model.User;
import com.talktranslate.model.dto.AuthResponse;
import com.talktranslate.model.dto.LoginRequest;
import com.talktranslate.model.dto.SignUpRequest;
import com.talktranslate.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    // --- Sign Up Tests (POST /api/auth/signup) ---

    @Test
    void shouldSignUpSuccessfully() throws Exception {
        SignUpRequest request = new SignUpRequest("rahul_sharma", "rahul@test.com", "Password123!", "Rahul Sharma");
        AuthResponse expectedResponse = AuthResponse.builder()
                .userId("usr_123")
                .username("rahul_sharma")
                .email("rahul@test.com")
                .fullName("Rahul Sharma")
                .preferredLanguage("en")
                .token("jwt_token_sample")
                .build();

        when(authService.signUp(any(SignUpRequest.class))).thenReturn(expectedResponse);

        MvcResult result = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse actualResponse = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);

        assertAll("Verify Sign Up AuthResponse",
                () -> assertNotNull(actualResponse),
                () -> assertThat(actualResponse.getUserId()).isEqualTo("usr_123"),
                () -> assertThat(actualResponse.getUsername()).isEqualTo("rahul_sharma"),
                () -> assertThat(actualResponse.getEmail()).isEqualTo("rahul@test.com"),
                () -> assertThat(actualResponse.getFullName()).isEqualTo("Rahul Sharma"),
                () -> assertThat(actualResponse.getPreferredLanguage()).isEqualTo("en"),
                () -> assertThat(actualResponse.getToken()).isEqualTo("jwt_token_sample")
        );

        verify(authService, times(1)).signUp(any(SignUpRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenSignUpThrowsIllegalArgumentException() throws Exception {
        SignUpRequest request = new SignUpRequest("", "rahul@test.com", "Password123!", "Rahul Sharma");

        when(authService.signUp(any(SignUpRequest.class)))
                .thenThrow(new IllegalArgumentException("Username is required and cannot be blank"));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Username is required and cannot be blank"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService, times(1)).signUp(any(SignUpRequest.class));
    }

    @Test
    void shouldReturnBadRequestWithDefaultMessageWhenSignUpExceptionMessageIsNull() throws Exception {
        SignUpRequest request = new SignUpRequest("user", "test@test.com", "pass", "Name");

        when(authService.signUp(any(SignUpRequest.class)))
                .thenThrow(new IllegalArgumentException((String) null));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Error"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService, times(1)).signUp(any(SignUpRequest.class));
    }

    @Test
    void shouldReturnConflictWhenSignUpThrowsIllegalStateException() throws Exception {
        SignUpRequest request = new SignUpRequest("rahul_sharma", "rahul@test.com", "Password123!", "Rahul Sharma");

        when(authService.signUp(any(SignUpRequest.class)))
                .thenThrow(new IllegalStateException("Username 'rahul_sharma' is already taken"));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Username 'rahul_sharma' is already taken"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService, times(1)).signUp(any(SignUpRequest.class));
    }

    @Test
    void shouldReturnConflictWithDefaultMessageWhenSignUpIllegalStateExceptionMessageIsNull() throws Exception {
        SignUpRequest request = new SignUpRequest("rahul_sharma", "rahul@test.com", "Password123!", "Rahul Sharma");

        when(authService.signUp(any(SignUpRequest.class)))
                .thenThrow(new IllegalStateException((String) null));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Error"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService, times(1)).signUp(any(SignUpRequest.class));
    }

    @Test
    void shouldReturnInternalServerErrorWhenSignUpFailsUnexpectedly() throws Exception {
        SignUpRequest request = new SignUpRequest("rahul_sharma", "rahul@test.com", "Password123!", "Rahul Sharma");

        when(authService.signUp(any(SignUpRequest.class)))
                .thenThrow(new RuntimeException("Database connection failure"));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected server error occurred: Database connection failure"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService, times(1)).signUp(any(SignUpRequest.class));
    }

    // --- Login Tests (POST /api/auth/login) ---

    @Test
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest request = new LoginRequest("rahul_sharma", "Password123!");
        AuthResponse expectedResponse = AuthResponse.builder()
                .userId("usr_123")
                .username("rahul_sharma")
                .email("rahul@test.com")
                .fullName("Rahul Sharma")
                .preferredLanguage("en")
                .token("jwt_token_sample")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(expectedResponse);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse actualResponse = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);

        assertAll("Verify Login AuthResponse",
                () -> assertNotNull(actualResponse),
                () -> assertThat(actualResponse.getUserId()).isEqualTo("usr_123"),
                () -> assertThat(actualResponse.getUsername()).isEqualTo("rahul_sharma"),
                () -> assertThat(actualResponse.getToken()).isEqualTo("jwt_token_sample")
        );

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenLoginThrowsIllegalArgumentException() throws Exception {
        LoginRequest request = new LoginRequest("", "Password123!");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new IllegalArgumentException("Username or Email is required"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Username or Email is required"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void shouldReturnBadRequestWithDefaultMessageWhenLoginExceptionMessageIsNull() throws Exception {
        LoginRequest request = new LoginRequest("user", "pass");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new IllegalArgumentException((String) null));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Error"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void shouldReturnUnauthorizedWhenLoginFailsBadCredentials() throws Exception {
        LoginRequest request = new LoginRequest("rahul_sharma", "WrongPass");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid username or password"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void shouldReturnUnauthorizedWithDefaultMessageWhenLoginBadCredentialsMessageIsNull() throws Exception {
        LoginRequest request = new LoginRequest("rahul_sharma", "WrongPass");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException(null));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Error"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void shouldReturnInternalServerErrorWhenLoginFailsUnexpectedly() throws Exception {
        LoginRequest request = new LoginRequest("rahul_sharma", "Password123!");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Service temporarily unavailable"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected server error occurred: Service temporarily unavailable"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    // --- Current User Tests (GET /api/auth/me) ---

    @Test
    void shouldGetCurrentUserProfileSuccessfully() throws Exception {
        User expectedUser = User.builder()
                .id("usr_123")
                .username("rahul_sharma")
                .email("rahul@test.com")
                .fullName("Rahul Sharma")
                .preferredLanguage("hi")
                .build();

        when(authService.getCurrentUser("Bearer valid_jwt_token")).thenReturn(expectedUser);

        MvcResult result = mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer valid_jwt_token"))
                .andExpect(status().isOk())
                .andReturn();

        User actualUser = objectMapper.readValue(result.getResponse().getContentAsString(), User.class);

        assertAll("Verify Current User Profile",
                () -> assertNotNull(actualUser),
                () -> assertThat(actualUser.getId()).isEqualTo("usr_123"),
                () -> assertThat(actualUser.getUsername()).isEqualTo("rahul_sharma"),
                () -> assertThat(actualUser.getPreferredLanguage()).isEqualTo("hi")
        );

        verify(authService, times(1)).getCurrentUser("Bearer valid_jwt_token");
    }

    @Test
    void shouldReturnUnauthorizedWhenAuthHeaderIsMissing() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Missing Authorization header"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService, never()).getCurrentUser(anyString());
    }

    @Test
    void shouldReturnUnauthorizedWhenAuthHeaderIsBlank() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "   "))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Missing Authorization header"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService, never()).getCurrentUser(anyString());
    }

    @Test
    void shouldReturnUnauthorizedWhenAuthHeaderIsEmpty() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", ""))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Missing Authorization header"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService, never()).getCurrentUser(anyString());
    }

    @Test
    void shouldReturnUnauthorizedWhenGetCurrentUserThrowsBadCredentialsException() throws Exception {
        when(authService.getCurrentUser("Bearer invalid_token"))
                .thenThrow(new BadCredentialsException("Invalid or expired authentication token"));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid or expired authentication token"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService, times(1)).getCurrentUser("Bearer invalid_token");
    }

    @Test
    void shouldReturnUnauthorizedWithDefaultMessageWhenGetCurrentUserBadCredentialsMessageIsNull() throws Exception {
        when(authService.getCurrentUser("Bearer invalid_token"))
                .thenThrow(new BadCredentialsException(null));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Error"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService, times(1)).getCurrentUser("Bearer invalid_token");
    }

    @Test
    void shouldReturnInternalServerErrorWhenGetCurrentUserFailsUnexpectedly() throws Exception {
        when(authService.getCurrentUser("Bearer valid_token"))
                .thenThrow(new RuntimeException("Redis connection refused"));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer valid_token"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected server error occurred: Redis connection refused"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService, times(1)).getCurrentUser("Bearer valid_token");
    }
}
