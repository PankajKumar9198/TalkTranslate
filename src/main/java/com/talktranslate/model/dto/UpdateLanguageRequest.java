package com.talktranslate.model.dto;

public class UpdateLanguageRequest {
    private String userId;
    private String languageCode; // e.g. "hi", "en", "es", "ja", "fr"

    public UpdateLanguageRequest() {
    }

    public UpdateLanguageRequest(String userId, String languageCode) {
        this.userId = userId;
        this.languageCode = languageCode;
    }

    public static UpdateLanguageRequestBuilder builder() {
        return new UpdateLanguageRequestBuilder();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public static class UpdateLanguageRequestBuilder {
        private String userId;
        private String languageCode;

        UpdateLanguageRequestBuilder() {
        }

        public UpdateLanguageRequestBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public UpdateLanguageRequestBuilder languageCode(String languageCode) {
            this.languageCode = languageCode;
            return this;
        }

        public UpdateLanguageRequest build() {
            return new UpdateLanguageRequest(userId, languageCode);
        }
    }
}
