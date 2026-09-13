package com.talktranslate.model;

public class SupportedLanguage {
    private String code;
    private String name;
    private String nativeName;
    private String flagEmoji;
    private String voiceLocale;

    public SupportedLanguage() {
    }

    public SupportedLanguage(String code, String name, String nativeName, String flagEmoji, String voiceLocale) {
        this.code = code;
        this.name = name;
        this.nativeName = nativeName;
        this.flagEmoji = flagEmoji;
        this.voiceLocale = voiceLocale;
    }

    public static SupportedLanguageBuilder builder() {
        return new SupportedLanguageBuilder();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNativeName() {
        return nativeName;
    }

    public void setNativeName(String nativeName) {
        this.nativeName = nativeName;
    }

    public String getFlagEmoji() {
        return flagEmoji;
    }

    public void setFlagEmoji(String flagEmoji) {
        this.flagEmoji = flagEmoji;
    }

    public String getVoiceLocale() {
        return voiceLocale;
    }

    public void setVoiceLocale(String voiceLocale) {
        this.voiceLocale = voiceLocale;
    }

    public static class SupportedLanguageBuilder {
        private String code;
        private String name;
        private String nativeName;
        private String flagEmoji;
        private String voiceLocale;

        SupportedLanguageBuilder() {
        }

        public SupportedLanguageBuilder code(String code) {
            this.code = code;
            return this;
        }

        public SupportedLanguageBuilder name(String name) {
            this.name = name;
            return this;
        }

        public SupportedLanguageBuilder nativeName(String nativeName) {
            this.nativeName = nativeName;
            return this;
        }

        public SupportedLanguageBuilder flagEmoji(String flagEmoji) {
            this.flagEmoji = flagEmoji;
            return this;
        }

        public SupportedLanguageBuilder voiceLocale(String voiceLocale) {
            this.voiceLocale = voiceLocale;
            return this;
        }

        public SupportedLanguage build() {
            return new SupportedLanguage(code, name, nativeName, flagEmoji, voiceLocale);
        }
    }
}
