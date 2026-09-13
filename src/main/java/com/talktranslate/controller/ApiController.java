package com.talktranslate.controller;

import com.talktranslate.model.SupportedLanguage;
import com.talktranslate.model.TranslationRequest;
import com.talktranslate.model.TranslationResponse;
import com.talktranslate.service.TranslationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final TranslationService translationService;

    public ApiController(TranslationService translationService) {
        this.translationService = translationService;
    }

    /**
     * Health check endpoint
     * GET /api/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "application", "TalkTranslate",
                "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Get list of supported languages
     * GET /api/languages
     */
    @GetMapping("/languages")
    public ResponseEntity<List<SupportedLanguage>> getSupportedLanguages() {
        return ResponseEntity.ok(translationService.getSupportedLanguages());
    }

    /**
     * On-demand text translation
     * POST /api/translate
     */
    @PostMapping("/translate")
    public ResponseEntity<TranslationResponse> translateText(@RequestBody(required = false) TranslationRequest request) {
        String text = request != null ? request.getText() : null;
        String sourceLang = request != null ? request.getSourceLang() : null;
        String targetLang = request != null ? request.getTargetLang() : null;
        TranslationResponse response = translationService.translate(
                text,
                sourceLang,
                targetLang
        );
        return ResponseEntity.ok(response);
    }
}
