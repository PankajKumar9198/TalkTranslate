package com.talktranslate.model;

public class TranslationResponse {
    private String originalText;
    private String translatedText;
    private String sourceLang;
    private String targetLang;
    private String detectedLang;
    private boolean cached;

    public TranslationResponse() {
    }

    public TranslationResponse(String originalText, String translatedText, String sourceLang,
                               String targetLang, String detectedLang, boolean cached) {
        this.originalText = originalText;
        this.translatedText = translatedText;
        this.sourceLang = sourceLang;
        this.targetLang = targetLang;
        this.detectedLang = detectedLang;
        this.cached = cached;
    }

    public static TranslationResponseBuilder builder() {
        return new TranslationResponseBuilder();
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
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

    public String getDetectedLang() {
        return detectedLang;
    }

    public void setDetectedLang(String detectedLang) {
        this.detectedLang = detectedLang;
    }

    public boolean isCached() {
        return cached;
    }

    public void setCached(boolean cached) {
        this.cached = cached;
    }

    public static class TranslationResponseBuilder {
        private String originalText;
        private String translatedText;
        private String sourceLang;
        private String targetLang;
        private String detectedLang;
        private boolean cached;

        TranslationResponseBuilder() {
        }

        public TranslationResponseBuilder originalText(String originalText) {
            this.originalText = originalText;
            return this;
        }

        public TranslationResponseBuilder translatedText(String translatedText) {
            this.translatedText = translatedText;
            return this;
        }

        public TranslationResponseBuilder sourceLang(String sourceLang) {
            this.sourceLang = sourceLang;
            return this;
        }

        public TranslationResponseBuilder targetLang(String targetLang) {
            this.targetLang = targetLang;
            return this;
        }

        public TranslationResponseBuilder detectedLang(String detectedLang) {
            this.detectedLang = detectedLang;
            return this;
        }

        public TranslationResponseBuilder cached(boolean cached) {
            this.cached = cached;
            return this;
        }

        public TranslationResponse build() {
            return new TranslationResponse(originalText, translatedText, sourceLang, targetLang, detectedLang, cached);
        }
    }
}
