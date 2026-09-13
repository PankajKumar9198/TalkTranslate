package com.talktranslate.model;

public class TranslationRequest {
    private String text;
    private String sourceLang;
    private String targetLang;

    public TranslationRequest() {
    }

    public TranslationRequest(String text, String sourceLang, String targetLang) {
        this.text = text;
        this.sourceLang = sourceLang;
        this.targetLang = targetLang;
    }

    public static TranslationRequestBuilder builder() {
        return new TranslationRequestBuilder();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSourceLang() {
        return sourceLang;
    }

    public void setSourceLang(String sourceLang) {
        this.sourceLang = sourceLang;
    }

    public String getTargetLang() {
        return targetLang;
    }

    public void setTargetLang(String targetLang) {
        this.targetLang = targetLang;
    }

    public static class TranslationRequestBuilder {
        private String text;
        private String sourceLang;
        private String targetLang;

        TranslationRequestBuilder() {
        }

        public TranslationRequestBuilder text(String text) {
            this.text = text;
            return this;
        }

        public TranslationRequestBuilder sourceLang(String sourceLang) {
            this.sourceLang = sourceLang;
            return this;
        }

        public TranslationRequestBuilder targetLang(String targetLang) {
            this.targetLang = targetLang;
            return this;
        }

        public TranslationRequest build() {
            return new TranslationRequest(text, sourceLang, targetLang);
        }
    }
}
