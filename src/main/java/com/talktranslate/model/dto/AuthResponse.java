package com.talktranslate.model.dto;

public class AuthResponse {
    private String token;
    private String userId;
    private String username;
    private String email;
    private String fullName;
    private String preferredLanguage;
    private String message;

    public AuthResponse() {
    }

    public AuthResponse(String token, String userId, String username, String email,
                        String fullName, String preferredLanguage, String message) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.preferredLanguage = preferredLanguage;
        this.message = message;
    }

    public static AuthResponseBuilder builder() {
        return new AuthResponseBuilder();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class AuthResponseBuilder {
        private String token;
        private String userId;
        private String username;
        private String email;
        private String fullName;
        private String preferredLanguage;
        private String message;

        AuthResponseBuilder() {
        }

        public AuthResponseBuilder token(String token) {
            this.token = token;
            return this;
        }

        public AuthResponseBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public AuthResponseBuilder username(String username) {
            this.username = username;
            return this;
        }

        public AuthResponseBuilder email(String email) {
            this.email = email;
            return this;
        }

        public AuthResponseBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public AuthResponseBuilder preferredLanguage(String preferredLanguage) {
            this.preferredLanguage = preferredLanguage;
            return this;
        }

        public AuthResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public AuthResponse build() {
            return new AuthResponse(token, userId, username, email, fullName, preferredLanguage, message);
        }
    }
}
