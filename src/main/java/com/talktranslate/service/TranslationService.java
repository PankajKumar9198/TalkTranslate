package com.talktranslate.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talktranslate.model.SupportedLanguage;
import com.talktranslate.model.TranslationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Service managing automatic language detection, translation caching, offline dictionary lookups,
 * and online translation APIs for multilingual messaging.
 */
@Service
public class TranslationService {

    private static final Logger logger = LoggerFactory.getLogger(TranslationService.class);
    private static final String DEFAULT_LANGUAGE = "en";

    private final ObjectMapper objectMapper = new ObjectMapper();

    // In-memory translation cache: "sourceLang:targetLang:normalizedText" -> translation
    private final Map<String, String> translationCache = new ConcurrentHashMap<>();

    private final List<SupportedLanguage> supportedLanguages = new ArrayList<>();
    private final Map<String, SupportedLanguage> languageMap = new HashMap<>();

    // Smart conversational fallback dictionary
    private final Map<String, Map<String, String>> phraseDictionary = new HashMap<>();

    /**
     * Encapsulates a declarative language detection rule.
     */
    private record DetectionRule(Pattern pattern, String languageCode) {
        public boolean matches(String text) {
            return pattern.matcher(text).find();
        }
    }

    // Pre-compiled, table-driven Unicode script detection rules
    private static final List<DetectionRule> SCRIPT_RULES = List.of(
            new DetectionRule(Pattern.compile("[\\u0900-\\u097F]"), "hi"), // Devanagari (Hindi / Marathi)
            new DetectionRule(Pattern.compile("[\\u0600-\\u06FF]"), "ar"), // Arabic
            new DetectionRule(Pattern.compile("[\\u3040-\\u309F\\u30A0-\\u30FF]"), "ja"), // Japanese (Hiragana, Katakana, Kanji)
            new DetectionRule(Pattern.compile("[\\uAC00-\\uD7AF\\u1100-\\u11FF]"), "ko"), // Korean (Hangul)
            new DetectionRule(Pattern.compile("[\\u4E00-\\u9FFF]"), "zh"), // Chinese (CJK Ideographs)
            new DetectionRule(Pattern.compile("[\\u0400-\\u04FF]"), "ru"), // Cyrillic (Russian)
            new DetectionRule(Pattern.compile("[\\u0980-\\u09FF]"), "bn"), // Bengali
            new DetectionRule(Pattern.compile("[\\u0B80-\\u0BFF]"), "ta"), // Tamil
            new DetectionRule(Pattern.compile("[\\u0C00-\\u0C7F]"), "te"), // Telugu
            new DetectionRule(Pattern.compile("[\\u0A80-\\u0AFF]"), "gu")  // Gujarati
    );

    // Pre-compiled, table-driven Latin conversational keyword rules
    private static final List<DetectionRule> KEYWORD_RULES = List.of(
            new DetectionRule(Pattern.compile("(?iu)\\b(kaise|kya|hai|hain|hoon|aap|tum|kahan|kyu|kyun|theek|bhai|shukriya|dhanyawad|accha|bahut|karo|rahe|raha|baat|nahi|karunga|karega|milte)\\b"), "hinglish"),
            new DetectionRule(Pattern.compile("(?iu)\\b(hola|buenos|gracias|amigo|por favor|cómo estás|bienvenido)\\b"), "es"),
            new DetectionRule(Pattern.compile("(?iu)\\b(bonjour|salut|merci|comment|s'il vous plaît|oui|bienvenue)\\b"), "fr"),
            new DetectionRule(Pattern.compile("(?iu)\\b(hallo|guten tag|danke|bitte|wie gehts|tschüss)\\b"), "de"),
            new DetectionRule(Pattern.compile("(?iu)\\b(ciao|grazie|prego|buongiorno|come stai|arrivederci)\\b"), "it"),
            new DetectionRule(Pattern.compile("(?iu)\\b(olá|obrigado|obrigada|como vai|bom dia|por favor)\\b"), "pt")
    );

    public TranslationService() {
        initSupportedLanguages();
        initPhraseDictionary();
    }

    private void initSupportedLanguages() {
        addLanguage("en", "English", "English", "🇺🇸", "en-US");
        addLanguage("hi", "Hindi", "हिन्दी", "🇮🇳", "hi-IN");
        addLanguage("hinglish", "Hinglish", "हिंग्लिश (Hinglish)", "🇮🇳", "hi-IN");
        addLanguage("es", "Spanish", "Español", "🇪🇸", "es-ES");
        addLanguage("fr", "French", "Français", "🇫🇷", "fr-FR");
        addLanguage("de", "German", "Deutsch", "🇩🇪", "de-DE");
        addLanguage("ja", "Japanese", "日本語", "🇯🇵", "ja-JP");
        addLanguage("zh", "Chinese (Simplified)", "简体中文", "🇨🇳", "zh-CN");
        addLanguage("ar", "Arabic", "العربية", "🇸🇦", "ar-SA");
        addLanguage("ru", "Russian", "Русский", "🇷🇺", "ru-RU");
        addLanguage("pt", "Portuguese", "Português", "🇧🇷", "pt-BR");
        addLanguage("it", "Italian", "Italiano", "🇮🇹", "it-IT");
        addLanguage("ko", "Korean", "한국어", "🇰🇷", "ko-KR");
        addLanguage("bn", "Bengali", "বাংলা", "🇧🇩", "bn-BD");
        addLanguage("tr", "Turkish", "Türkçe", "🇹🇷", "tr-TR");
        addLanguage("vi", "Vietnamese", "Tiếng Việt", "🇻🇳", "vi-VN");
        addLanguage("id", "Indonesian", "Bahasa Indonesia", "🇮🇩", "id-ID");
        addLanguage("nl", "Dutch", "Nederlands", "🇳🇱", "nl-NL");
        addLanguage("pl", "Polish", "Polski", "🇵🇱", "pl-PL");
        addLanguage("ta", "Tamil", "தமிழ்", "🇮🇳", "ta-IN");
        addLanguage("te", "Telugu", "తెలుగు", "🇮🇳", "te-IN");
        addLanguage("mr", "Marathi", "मराठी", "🇮🇳", "mr-IN");
        addLanguage("gu", "Gujarati", "ગુજરાતી", "🇮🇳", "gu-IN");
    }

    private void addLanguage(String code, String name, String nativeName, String flag, String voiceLocale) {
        SupportedLanguage lang = new SupportedLanguage(code, name, nativeName, flag, voiceLocale);
        supportedLanguages.add(lang);
        languageMap.put(code.toLowerCase(), lang);
    }

    /**
     * Returns the complete list of supported languages.
     *
     * @return unmodifiable list of supported languages
     */
    public List<SupportedLanguage> getSupportedLanguages() {
        return Collections.unmodifiableList(supportedLanguages);
    }

    /**
     * Looks up supported language metadata by ISO code.
     *
     * @param code the language code
     * @return matching SupportedLanguage or English default if not found
     */
    public SupportedLanguage getLanguage(String code) {
        return languageMap.getOrDefault(code != null ? code.toLowerCase() : DEFAULT_LANGUAGE, languageMap.get(DEFAULT_LANGUAGE));
    }

    /**
     * Checks if a language code is currently supported by the translation engine.
     *
     * @param code the language code to check
     * @return true if supported; false otherwise
     */
    public boolean isLanguageSupported(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        return languageMap.containsKey(code.trim().toLowerCase());
    }

    /**
     * Translates text from source language into recipient's target language using a structured lookup pipeline.
     *
     * @param text the message text
     * @param sourceLang the source language code or "auto"
     * @param targetLang the target recipient language code
     * @return the translation response object
     */
    public TranslationResponse translate(String text, String sourceLang, String targetLang) {
        if (text == null || text.trim().isEmpty()) {
            return new TranslationResponse("", "", sourceLang, targetLang, sourceLang, false);
        }

        String cleanedText = text.trim();
        String detected = (sourceLang == null || "auto".equalsIgnoreCase(sourceLang))
                ? detectLanguage(cleanedText)
                : sourceLang.toLowerCase();
        String target = (targetLang == null || targetLang.trim().isEmpty())
                ? DEFAULT_LANGUAGE
                : targetLang.toLowerCase();

        // If source and target are the same language, no translation needed
        if (detected.equalsIgnoreCase(target)) {
            return new TranslationResponse(cleanedText, cleanedText, detected, target, detected, true);
        }

        String cacheKey = detected + ":" + target + ":" + cleanedText;

        // Step 1: Check in-memory cache
        String cached = translationCache.get(cacheKey);
        if (cached != null) {
            logger.debug("Translation cache hit for key: {}", cacheKey);
            return new TranslationResponse(cleanedText, cached, detected, target, detected, true);
        }

        // Step 2: Check offline phrase dictionary
        Optional<String> dictMatch = lookupDictionary(cleanedText, target);
        if (dictMatch.isPresent()) {
            String translated = dictMatch.get();
            logger.debug("Translation dictionary match found for: '{}' ({}) -> ({})", cleanedText, detected, target);
            translationCache.put(cacheKey, translated);
            return new TranslationResponse(cleanedText, translated, detected, target, detected, false);
        }

        // Step 3: Online translation APIs (Google Translate -> MyMemory fallback)
        Optional<String> onlineTranslation = callOnlineTranslationApi(cleanedText, detected, target);
        if (onlineTranslation.isPresent()) {
            String translated = onlineTranslation.get();
            logger.debug("Online translation completed for: '{}' ({}) -> ({})", cleanedText, detected, target);
            translationCache.put(cacheKey, translated);
            return new TranslationResponse(cleanedText, translated, detected, target, detected, false);
        }

        // Step 4: Fallback - Return original text
        logger.warn("Translation unavailable for text '{}' from {} to {}. Returning original.", cleanedText, detected, target);
        return new TranslationResponse(cleanedText, cleanedText, detected, target, detected, false);
    }

    /**
     * Script & keyword-based automatic language detection using pre-compiled, table-driven rules.
     *
     * @param text the input text to detect language for
     * @return ISO language code
     */
    public String detectLanguage(String text) {
        if (text == null || text.isBlank()) {
            return DEFAULT_LANGUAGE;
        }

        return SCRIPT_RULES.stream()
                .filter(rule -> rule.matches(text))
                .map(DetectionRule::languageCode)
                .findFirst()
                .orElseGet(() -> KEYWORD_RULES.stream()
                        .filter(rule -> rule.matches(text))
                        .map(DetectionRule::languageCode)
                        .findFirst()
                        .orElse(DEFAULT_LANGUAGE));
    }

    private Optional<String> callOnlineTranslationApi(String text, String sourceLang, String targetLang) {
        String apiSource = "hinglish".equalsIgnoreCase(sourceLang) ? "hi" : sourceLang;
        String apiTarget = "hinglish".equalsIgnoreCase(targetLang) ? "hi" : targetLang;

        return fetchGoogleTranslation(text, apiSource, apiTarget)
                .or(() -> fetchMyMemoryTranslation(text, apiSource, apiTarget));
    }

    private Optional<String> fetchGoogleTranslation(String text, String source, String target) {
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
        String url = String.format(
                "https://translate.googleapis.com/translate_a/single?client=gtx&sl=%s&tl=%s&dt=t&q=%s",
                source, target, encodedText);

        return executeHttpGetJson(url)
                .filter(JsonNode::isArray)
                .map(root -> root.path(0))
                .filter(JsonNode::isArray)
                .map(segments -> {
                    StringBuilder result = new StringBuilder();
                    for (JsonNode part : segments) {
                        if (part.isArray() && !part.isEmpty()) {
                            result.append(part.get(0).asText());
                        }
                    }
                    return result.toString();
                })
                .filter(result -> !result.isBlank());
    }

    private Optional<String> fetchMyMemoryTranslation(String text, String source, String target) {
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
        String url = "https://api.mymemory.translated.net/get?q=" + encodedText + "&langpair=" + source + "|" + target;

        return executeHttpGetJson(url)
                .map(root -> root.path("responseData").path("translatedText").asText(null))
                .filter(translated -> translated != null && !translated.isBlank());
    }

    private Optional<JsonNode> executeHttpGetJson(String urlStr) {
        HttpURLConnection conn = null;
        try {
            URI uri = URI.create(urlStr);
            conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            if (conn.getResponseCode() == 200) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    return Optional.ofNullable(objectMapper.readTree(reader));
                }
            }
        } catch (Exception e) {
            logger.debug("HTTP GET request failed for URL {}: {}", urlStr, e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return Optional.empty();
    }

    private Optional<String> lookupDictionary(String text, String target) {
        String normalized = text.trim().toLowerCase().replaceAll("[!?,.]", "");
        Map<String, String> translations = phraseDictionary.get(normalized);
        return Optional.ofNullable(translations).map(map -> map.get(target));
    }

    private void initPhraseDictionary() {
        addPhraseGroup(
                "en", "Hello, how are you?",
                "hi", "नमस्ते, आप कैसे हैं?",
                "hinglish", "Namaste, aap kaise hain?",
                "es", "Hola, ¿cómo estás?",
                "fr", "Bonjour, comment allez-vous?",
                "de", "Hallo, wie geht es dir?",
                "ja", "こんにちは、お元気ですか？",
                "zh", "你好，你好吗？",
                "ar", "مرحبا كيف حالك؟",
                "ru", "Привет, как дела?",
                "pt", "Olá, como você está?"
        );

        addPhraseGroup(
                "en", "I am doing well, thank you!",
                "hi", "मैं ठीक हूँ, धन्यवाद!",
                "hinglish", "Main theek hoon, shukriya!",
                "es", "¡Estoy bien, gracias!",
                "fr", "Je vais bien, merci!",
                "de", "Mir geht es gut, danke!",
                "ja", "元気です、ありがとう！",
                "zh", "我很好，谢谢！",
                "ar", "أنا بخير، شكرا لك!",
                "ru", "Я в порядке, спасибо!",
                "pt", "Estou bem, obrigado!"
        );

        addPhraseGroup(
                "en", "Nice to meet you",
                "hi", "आपसे मिलकर अच्छा लगा",
                "hinglish", "Aapse milkar accha laga",
                "es", "Mucho gusto en conocerte",
                "fr", "Enchanté de vous rencontrer",
                "de", "Schön dich kennenzulernen",
                "ja", "はじめまして",
                "zh", "很高兴认识你",
                "ar", "سعيد بلقائك",
                "ru", "Приятно познакомиться",
                "pt", "Prazer em conhecê-lo"
        );

        addPhraseGroup(
                "en", "Good morning",
                "hi", "शुभ प्रभात",
                "hinglish", "Shubh prabhat / Good morning",
                "es", "Buenos días",
                "fr", "Bonjour",
                "de", "Guten Morgen",
                "ja", "おはようございます",
                "zh", "早上好",
                "ar", "صباح الخير",
                "ru", "Доброе утро",
                "pt", "Bom dia"
        );

        addPhraseGroup(
                "en", "Goodbye and take care",
                "hi", "अलविदा और अपना ख्याल रखें",
                "hinglish", "Alvida aur apna khayal rakhna",
                "es", "Adiós y cuídate",
                "fr", "Au revoir et prends soin de toi",
                "de", "Auf Wiedersehen und pass auf dich auf",
                "ja", "さようなら、お元気で",
                "zh", "再见，保重",
                "ar", "وداعا واعتني بنفسك",
                "ru", "До свидания и береги себя",
                "pt", "Adeus e cuide-se"
        );
    }

    private void addPhraseGroup(String... langAndPhrases) {
        Map<String, String> phraseMap = new HashMap<>();
        for (int i = 0; i < langAndPhrases.length - 1; i += 2) {
            phraseMap.put(langAndPhrases[i], langAndPhrases[i + 1]);
        }
        for (Map.Entry<String, String> entry : phraseMap.entrySet()) {
            String key = entry.getValue().trim().toLowerCase().replaceAll("[!?,.]", "");
            phraseDictionary.put(key, phraseMap);
        }
    }
}
