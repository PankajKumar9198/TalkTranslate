package com.talktranslate.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class SupportedLanguageTest {

    @Test
    void shouldInitializeDefaultConstructorWithNullFields() {
        SupportedLanguage lang = new SupportedLanguage();

        assertAll("Verify default constructor field values",
                () -> assertThat(lang.getCode()).isNull(),
                () -> assertThat(lang.getName()).isNull(),
                () -> assertThat(lang.getNativeName()).isNull(),
                () -> assertThat(lang.getFlagEmoji()).isNull(),
                () -> assertThat(lang.getVoiceLocale()).isNull()
        );
    }

    @Test
    void shouldInitializeViaAllArgsConstructor() {
        SupportedLanguage lang = new SupportedLanguage("es", "Spanish", "Español", "🇪🇸", "es-ES");

        assertAll("Verify all-args constructor field values",
                () -> assertThat(lang.getCode()).isEqualTo("es"),
                () -> assertThat(lang.getName()).isEqualTo("Spanish"),
                () -> assertThat(lang.getNativeName()).isEqualTo("Español"),
                () -> assertThat(lang.getFlagEmoji()).isEqualTo("🇪🇸"),
                () -> assertThat(lang.getVoiceLocale()).isEqualTo("es-ES")
        );
    }

    @Test
    void shouldSetAndGetFieldsViaSetters() {
        SupportedLanguage lang = new SupportedLanguage();
        lang.setCode("fr");
        lang.setName("French");
        lang.setNativeName("Français");
        lang.setFlagEmoji("🇫🇷");
        lang.setVoiceLocale("fr-FR");

        assertAll("Verify setter modifications",
                () -> assertThat(lang.getCode()).isEqualTo("fr"),
                () -> assertThat(lang.getName()).isEqualTo("French"),
                () -> assertThat(lang.getNativeName()).isEqualTo("Français"),
                () -> assertThat(lang.getFlagEmoji()).isEqualTo("🇫🇷"),
                () -> assertThat(lang.getVoiceLocale()).isEqualTo("fr-FR")
        );
    }

    @Test
    void shouldBuildSupportedLanguageWithCompleteBuilder() {
        SupportedLanguage lang = SupportedLanguage.builder()
                .code("hi")
                .name("Hindi")
                .nativeName("हिन्दी")
                .flagEmoji("🇮🇳")
                .voiceLocale("hi-IN")
                .build();

        assertAll("Verify complete builder fields",
                () -> assertThat(lang.getCode()).isEqualTo("hi"),
                () -> assertThat(lang.getName()).isEqualTo("Hindi"),
                () -> assertThat(lang.getNativeName()).isEqualTo("हिन्दी"),
                () -> assertThat(lang.getFlagEmoji()).isEqualTo("🇮🇳"),
                () -> assertThat(lang.getVoiceLocale()).isEqualTo("hi-IN")
        );
    }

    @Test
    void shouldBuildSupportedLanguageWithEmptyBuilder() {
        SupportedLanguage lang = SupportedLanguage.builder().build();

        assertAll("Verify empty builder fields are null",
                () -> assertThat(lang.getCode()).isNull(),
                () -> assertThat(lang.getName()).isNull(),
                () -> assertThat(lang.getNativeName()).isNull(),
                () -> assertThat(lang.getFlagEmoji()).isNull(),
                () -> assertThat(lang.getVoiceLocale()).isNull()
        );
    }

    @Test
    void shouldBuildSupportedLanguageWithPartialBuilder() {
        SupportedLanguage lang = SupportedLanguage.builder()
                .code("de")
                .name("German")
                .build();

        assertAll("Verify partial builder fields",
                () -> assertThat(lang.getCode()).isEqualTo("de"),
                () -> assertThat(lang.getName()).isEqualTo("German"),
                () -> assertThat(lang.getNativeName()).isNull(),
                () -> assertThat(lang.getFlagEmoji()).isNull(),
                () -> assertThat(lang.getVoiceLocale()).isNull()
        );
    }

    @Test
    void shouldHandleNullAndEmptyValuesGracefully() {
        SupportedLanguage lang = new SupportedLanguage();
        lang.setCode("");
        lang.setName(null);
        lang.setNativeName("");
        lang.setFlagEmoji(null);
        lang.setVoiceLocale("");

        assertAll("Verify null and empty assignments",
                () -> assertThat(lang.getCode()).isEmpty(),
                () -> assertThat(lang.getName()).isNull(),
                () -> assertThat(lang.getNativeName()).isEmpty(),
                () -> assertThat(lang.getFlagEmoji()).isNull(),
                () -> assertThat(lang.getVoiceLocale()).isEmpty()
        );
    }

    @Test
    void shouldPreserveUnicodeAndEmojiCharacters() {
        SupportedLanguage lang = SupportedLanguage.builder()
                .code("ja")
                .name("Japanese")
                .nativeName("日本語")
                .flagEmoji("🇯🇵")
                .voiceLocale("ja-JP")
                .build();

        assertAll("Verify Unicode character preservation",
                () -> assertThat(lang.getCode()).isEqualTo("ja"),
                () -> assertThat(lang.getName()).isEqualTo("Japanese"),
                () -> assertThat(lang.getNativeName()).isEqualTo("日本語"),
                () -> assertThat(lang.getFlagEmoji()).isEqualTo("🇯🇵"),
                () -> assertThat(lang.getVoiceLocale()).isEqualTo("ja-JP")
        );
    }
}
