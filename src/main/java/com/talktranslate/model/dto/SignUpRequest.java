package com.talktranslate.model.dto;

public class SignUpRequest {
    private String username;
    private String email;
    private String password;
    private String fullName;

    public SignUpRequest() {
    }

    public SignUpRequest(String username, String email, String password, String fullName) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }

    public static SignUpRequestBuilder builder() {
        return new SignUpRequestBuilder();
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public static class SignUpRequestBuilder {
        private String username;
        private String email;
        private String password;
        private String fullName;

        SignUpRequestBuilder() {
        }

        public SignUpRequestBuilder username(String username) {
            this.username = username;
            return this;
        }

        public SignUpRequestBuilder email(String email) {
            this.email = email;
            return this;
        }

        public SignUpRequestBuilder password(String password) {
            this.password = password;
            return this;
        }

        public SignUpRequestBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public SignUpRequest build() {
            return new SignUpRequest(username, email, password, fullName);
        }
    }
}
