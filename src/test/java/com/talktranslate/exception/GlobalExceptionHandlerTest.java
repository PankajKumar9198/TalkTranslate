package com.talktranslate.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void shouldHandleBadCredentialsWithUnauthorizedStatusAndErrorSchema() {
        ResponseEntity<Map<String, Object>> response =
                exceptionHandler.handleBadCredentials(new BadCredentialsException("Invalid password"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(401);
        assertThat(response.getBody().get("error")).isEqualTo("Unauthorized");
        assertThat(response.getBody().get("message")).isEqualTo("Invalid password");
        assertThat(response.getBody().get("timestamp")).isNotNull();
        assertThat(Instant.parse(response.getBody().get("timestamp").toString())).isNotNull();
    }

    @Test
    void shouldHandleBadCredentialsWithCause() {
        Throwable cause = new RuntimeException("Root credential failure");
        ResponseEntity<Map<String, Object>> response =
                exceptionHandler.handleBadCredentials(new BadCredentialsException("Invalid token signature", cause));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(401);
        assertThat(response.getBody().get("error")).isEqualTo("Unauthorized");
        assertThat(response.getBody().get("message")).isEqualTo("Invalid token signature");
        assertThat(response.getBody().get("timestamp")).isNotNull();
    }

    @Test
    void shouldHandleBadCredentialsWithNullMessage() {
        ResponseEntity<Map<String, Object>> response =
                exceptionHandler.handleBadCredentials(new BadCredentialsException(null));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(401);
        assertThat(response.getBody().get("error")).isEqualTo("Unauthorized");
        assertThat(response.getBody().get("message")).isEqualTo("Error");
        assertThat(response.getBody().get("timestamp")).isNotNull();
    }

    @Test
    void shouldHandleNotFoundWithNotFoundStatusAndErrorSchema() {
        ResponseEntity<Map<String, Object>> response =
                exceptionHandler.handleNotFound(new NoSuchElementException("User not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(404);
        assertThat(response.getBody().get("error")).isEqualTo("Not Found");
        assertThat(response.getBody().get("message")).isEqualTo("User not found");
        assertThat(response.getBody().get("timestamp")).isNotNull();
    }

    @Test
    void shouldHandleNotFoundWithNullMessage() {
        ResponseEntity<Map<String, Object>> response =
                exceptionHandler.handleNotFound(new NoSuchElementException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(404);
        assertThat(response.getBody().get("error")).isEqualTo("Not Found");
        assertThat(response.getBody().get("message")).isEqualTo("Error");
        assertThat(response.getBody().get("timestamp")).isNotNull();
    }

    @Test
    void shouldHandleNoResourceFoundWithGuidanceMessage() {
        NoResourceFoundException exception = new NoResourceFoundException(HttpMethod.GET, "/api/unknown-endpoint");
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleNoResourceFound(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(404);
        assertThat(response.getBody().get("error")).isEqualTo("Not Found");
        assertThat(response.getBody().get("message"))
                .isEqualTo("Resource not found: /api/unknown-endpoint. Please check API path (e.g., /api/health, /actuator/health, /api/languages).");
        assertThat(response.getBody().get("timestamp")).isNotNull();
    }

    @Test
    void shouldHandleBadRequestWithBadRequestStatusAndErrorSchema() {
        ResponseEntity<Map<String, Object>> response =
                exceptionHandler.handleBadRequest(new IllegalArgumentException("Invalid param"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("Bad Request");
        assertThat(response.getBody().get("message")).isEqualTo("Invalid param");
        assertThat(response.getBody().get("timestamp")).isNotNull();
    }

    @Test
    void shouldHandleBadRequestWithNullMessage() {
        ResponseEntity<Map<String, Object>> response =
                exceptionHandler.handleBadRequest(new IllegalArgumentException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("Bad Request");
        assertThat(response.getBody().get("message")).isEqualTo("Error");
        assertThat(response.getBody().get("timestamp")).isNotNull();
    }

    @Test
    void shouldHandleConflictWithConflictStatusAndErrorSchema() {
        ResponseEntity<Map<String, Object>> response =
                exceptionHandler.handleConflict(new IllegalStateException("Already friends"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(409);
        assertThat(response.getBody().get("error")).isEqualTo("Conflict");
        assertThat(response.getBody().get("message")).isEqualTo("Already friends");
        assertThat(response.getBody().get("timestamp")).isNotNull();
    }

    @Test
    void shouldHandleConflictWithNullMessage() {
        ResponseEntity<Map<String, Object>> response =
                exceptionHandler.handleConflict(new IllegalStateException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(409);
        assertThat(response.getBody().get("error")).isEqualTo("Conflict");
        assertThat(response.getBody().get("message")).isEqualTo("Error");
        assertThat(response.getBody().get("timestamp")).isNotNull();
    }

    @Test
    void shouldHandleGeneralExceptionWithInternalServerErrorStatusAndFormattedMessage() {
        ResponseEntity<Map<String, Object>> response =
                exceptionHandler.handleGeneralException(new RuntimeException("System error"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(500);
        assertThat(response.getBody().get("error")).isEqualTo("Internal Server Error");
        assertThat(response.getBody().get("message")).isEqualTo("An unexpected server error occurred: System error");
        assertThat(response.getBody().get("timestamp")).isNotNull();
    }

    @Test
    void shouldHandleGeneralExceptionWithNullMessage() {
        ResponseEntity<Map<String, Object>> response =
                exceptionHandler.handleGeneralException(new NullPointerException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(500);
        assertThat(response.getBody().get("error")).isEqualTo("Internal Server Error");
        assertThat(response.getBody().get("message")).isEqualTo("An unexpected server error occurred: null");
        assertThat(response.getBody().get("timestamp")).isNotNull();
    }
}
