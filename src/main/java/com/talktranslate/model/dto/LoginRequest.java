package com.talktranslate.model.dto;

public class LoginRequest {
    private String usernameOrEmail;
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String usernameOrEmail, String password) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }

    public static LoginRequestBuilder builder() {
        return new LoginRequestBuilder();
    }

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static class LoginRequestBuilder {
        private String usernameOrEmail;
        private String password;

        LoginRequestBuilder() {
        }

        public LoginRequestBuilder usernameOrEmail(String usernameOrEmail) {
            this.usernameOrEmail = usernameOrEmail;
            return this;
        }

        public LoginRequestBuilder password(String password) {
            this.password = password;
            return this;
        }

        public LoginRequest build() {
            return new LoginRequest(usernameOrEmail, password);
        }
    }
}
