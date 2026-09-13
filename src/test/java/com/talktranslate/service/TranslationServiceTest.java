package com.talktranslate.service;

import com.talktranslate.model.SupportedLanguage;
import com.talktranslate.model.TranslationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TranslationServiceTest {

    private TranslationService translationService;

    @BeforeEach
    void setUp() {
        translationService = new TranslationService();
    }

    @Test
    void shouldReturnAllSupportedLanguages() {
        List<SupportedLanguage> languages = translationService.getSupportedLanguages();

        assertThat(languages).isNotNull().hasSize(23);
        assertThat(languages).allSatisfy(lang -> {
            assertThat(lang.getCode()).isNotBlank();
            assertThat(lang.getName()).isNotBlank();
            assertThat(lang.getNativeName()).isNotBlank();
            assertThat(lang.getFlagEmoji()).isNotBlank();
            assertThat(lang.getVoiceLocale()).isNotBlank();
        });

        assertThat(languages)
                .extracting(SupportedLanguage::getCode)
                .contains("en", "hi", "hinglish", "es", "fr", "de", "ja", "zh",
                        "ar", "ru", "pt", "it", "ko", "bn", "tr", "vi", "id",
                        "nl", "pl", "ta", "te", "mr", "gu");
    }

    @ParameterizedTest
    @CsvSource({
            "en, English",
            "EN, English",
            "hi, Hindi",
            "HI, Hindi",
            "hinglish, Hinglish",
            "Hinglish, Hinglish",
            "es, Spanish",
            "fr, French",
            "de, German",
            "ja, Japanese",
            "zh, Chinese (Simplified)",
            "ar, Arabic",
            "ru, Russian",
            "pt, Portuguese",
            "it, Italian",
            "ko, Korean",
            "bn, Bengali",
            "tr, Turkish",
            "vi, Vietnamese",
            "id, Indonesian",
            "nl, Dutch",
            "pl, Polish",
            "ta, Tamil",
            "te, Telugu",
            "mr, Marathi",
            "gu, Gujarati"
    })
    void shouldRetrieveLanguageByValidCode(String code, String expectedName) {
        SupportedLanguage language = translationService.getLanguage(code);

        assertThat(language).isNotNull();
        assertThat(language.getName()).isEqualTo(expectedName);
    }

    @Test
    void shouldFallbackToEnglishForUnknownCode() {
        SupportedLanguage fallback = translationService.getLanguage("unknown-xyz");

        assertThat(fallback).isNotNull();
        assertThat(fallback.getCode()).isEqualTo("en");
        assertThat(fallback.getName()).isEqualTo("English");
    }

    @Test
    void shouldFallbackToEnglishForNullCode() {
        SupportedLanguage fallback = translationService.getLanguage(null);

        assertThat(fallback).isNotNull();
        assertThat(fallback.getCode()).isEqualTo("en");
        assertThat(fallback.getName()).isEqualTo("English");
    }

    @ParameterizedTest
    @CsvSource({
            "'नमस्ते दुनिया', hi",
            "'शुभ प्रभात', hi",
            "'مرحبا كيف حالك', ar",
            "'صباح الخير', ar",
            "'こんにちは', ja",
            "'おはようございます', ja",
            "'안녕하세요', ko",
            "'감사합니다', ko",
            "'你好，世界', zh",
            "'早上好', zh",
            "'Привет как дела', ru",
            "'Доброе утро', ru",
            "'নমস্কার কেমন আছেন', bn",
            "'வணக்கம் எப்படி இருக்கிறீர்கள்', ta",
            "'నమస్కారం ఎలా ఉన్నారు', te",
            "'નમસ્તે કેમ છો', gu"
    })
    void shouldDetectUnicodeScriptLanguages(String text, String expectedCode) {
        String detected = translationService.detectLanguage(text);
        assertThat(detected).isEqualTo(expectedCode);
    }

    @ParameterizedTest
    @CsvSource({
            "'Aap kaise ho bhai?', hinglish",
            "'Main theek hoon shukriya', hinglish",
            "'Kya baat hai, milte hain', hinglish",
            "'Hola amigo, ¿cómo estás?', es",
            "'Buenos días y muchas gracias', es",
            "'Bonjour comment allez-vous?', fr",
            "'Salut mon ami, merci beaucoup', fr",
            "'Hallo wie gehts dir?', de",
            "'Guten Tag, danke schön', de",
            "'Ciao come stai amico?', it",
            "'Buongiorno, grazie mille', it",
            "'Olá como vai você?', pt",
            "'Bom dia, muito obrigado', pt"
    })
    void shouldDetectLatinConversationalLanguages(String text, String expectedCode) {
        String detected = translationService.detectLanguage(text);
        assertThat(detected).isEqualTo(expectedCode);
    }

    @Test
    void shouldDefaultToEnglishForGenericText() {
        String detected = translationService.detectLanguage("The quick brown fox jumps over the lazy dog");
        assertThat(detected).isEqualTo("en");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   ", "\t", "\n" })
    void shouldReturnEnglishForNullOrBlankInput(String input) {
        String detected = translationService.detectLanguage(input);
        assertThat(detected).isEqualTo("en");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   ", "\t", "\n" })
    void shouldHandleNullOrBlankInputText(String input) {
        TranslationResponse response = translationService.translate(input, "en", "hi");

        assertThat(response).isNotNull();
        assertThat(response.getOriginalText()).isEmpty();
        assertThat(response.getTranslatedText()).isEmpty();
        assertThat(response.getSourceLang()).isEqualTo("en");
        assertThat(response.getTargetLang()).isEqualTo("hi");
        assertThat(response.isCached()).isFalse();
    }

    @Test
    void shouldReturnOriginalTextWhenLanguagesMatch() {
        TranslationResponse response = translationService.translate("Hello world", "en", "en");

        assertThat(response).isNotNull();
        assertThat(response.getOriginalText()).isEqualTo("Hello world");
        assertThat(response.getTranslatedText()).isEqualTo("Hello world");
        assertThat(response.getSourceLang()).isEqualTo("en");
        assertThat(response.getTargetLang()).isEqualTo("en");
        assertThat(response.getDetectedLang()).isEqualTo("en");
        assertThat(response.isCached()).isTrue();
    }

    @Test
    void shouldAutoDetectAndReturnOriginalTextWhenDetectedMatchesTarget() {
        TranslationResponse response = translationService.translate("Hello world", "auto", "en");

        assertThat(response).isNotNull();
        assertThat(response.getOriginalText()).isEqualTo("Hello world");
        assertThat(response.getTranslatedText()).isEqualTo("Hello world");
        assertThat(response.getDetectedLang()).isEqualTo("en");
        assertThat(response.getTargetLang()).isEqualTo("en");
        assertThat(response.isCached()).isTrue();
    }

    @Test
    void shouldAutoDetectSourceLanguageWhenSourceIsNull() {
        TranslationResponse response = translationService.translate("नमस्ते, आप कैसे हैं?", null, "en");

        assertThat(response).isNotNull();
        assertThat(response.getOriginalText()).isEqualTo("नमस्ते, आप कैसे हैं?");
        assertThat(response.getTranslatedText()).isEqualTo("Hello, how are you?");
        assertThat(response.getDetectedLang()).isEqualTo("hi");
        assertThat(response.getTargetLang()).isEqualTo("en");
    }

    @Test
    void shouldFallbackTargetLanguageToEnglishWhenTargetIsNull() {
        TranslationResponse response = translationService.translate("Hola, ¿cómo estás?", "es", null);

        assertThat(response).isNotNull();
        assertThat(response.getOriginalText()).isEqualTo("Hola, ¿cómo estás?");
        assertThat(response.getTranslatedText()).isEqualTo("Hello, how are you?");
        assertThat(response.getTargetLang()).isEqualTo("en");
        assertThat(response.getSourceLang()).isEqualTo("es");
    }

    @ParameterizedTest
    @CsvSource({
            "'Hello, how are you?', en, hi, 'नमस्ते, आप कैसे हैं?'",
            "'Hello, how are you?', en, hinglish, 'Namaste, aap kaise hain?'",
            "'Hello, how are you?', en, es, 'Hola, ¿cómo estás?'",
            "'Hello, how are you?', en, fr, 'Bonjour, comment allez-vous?'",
            "'Hello, how are you?', en, de, 'Hallo, wie geht es dir?'",
            "'Hello, how are you?', en, ja, 'こんにちは、お元気ですか？'",
            "'Hello, how are you?', en, zh, '你好，你好吗？'",
            "'Hello, how are you?', en, ar, 'مرحبا كيف حالك؟'",
            "'Hello, how are you?', en, ru, 'Привет, как дела?'",
            "'Hello, how are you?', en, pt, 'Olá, como você está?'",
            "'I am doing well, thank you!', en, hi, 'मैं ठीक हूँ, धन्यवाद!'",
            "'Nice to meet you', en, hi, 'आपसे मिलकर अच्छा लगा'",
            "'Good morning', en, hi, 'शुभ प्रभात'",
            "'Goodbye and take care', en, hi, 'अलविदा और अपना ख्याल रखें'"
    })
    void shouldTranslateKnownOfflineDictionaryPhrases(String phrase, String source, String target, String expected) {
        TranslationResponse response = translationService.translate(phrase, source, target);

        assertThat(response).isNotNull();
        assertThat(response.getOriginalText()).isEqualTo(phrase);
        assertThat(response.getTranslatedText()).isEqualTo(expected);
        assertThat(response.getSourceLang()).isEqualTo(source);
        assertThat(response.getTargetLang()).isEqualTo(target);
    }

    @Test
    void shouldMatchDictionaryPhraseIgnoringPunctuationAndCase() {
        TranslationResponse response1 = translationService.translate("good morning!", "en", "hi");
        TranslationResponse response2 = translationService.translate("Good Morning.", "en", "hi");

        assertThat(response1.getTranslatedText()).isEqualTo("शुभ प्रभात");
        assertThat(response2.getTranslatedText()).isEqualTo("शुभ प्रभात");
    }

    @Test
    void shouldPopulateAndHitTranslationCache() {
        String phrase = "Nice to meet you";

        TranslationResponse firstCall = translationService.translate(phrase, "en", "hi");
        assertThat(firstCall).isNotNull();
        assertThat(firstCall.getTranslatedText()).isEqualTo("आपसे मिलकर अच्छा लगा");
        assertThat(firstCall.isCached()).isFalse();

        TranslationResponse secondCall = translationService.translate(phrase, "en", "hi");
        assertThat(secondCall).isNotNull();
        assertThat(secondCall.getTranslatedText()).isEqualTo("आपसे मिलकर अच्छा लगा");
        assertThat(secondCall.isCached()).isTrue();
    }

    @Test
    void shouldTrimWhitespaceFromInputText() {
        TranslationResponse response = translationService.translate("   Good morning   ", "en", "hi");

        assertThat(response).isNotNull();
        assertThat(response.getOriginalText()).isEqualTo("Good morning");
        assertThat(response.getTranslatedText()).isEqualTo("शुभ प्रभात");
    }
}
