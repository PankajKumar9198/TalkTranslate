package com.talktranslate.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> rootInfo() {
        return ResponseEntity.ok(Map.of(
                "application", "TalkTranslate API",
                "version", "1.0.0",
                "status", "UP",
                "description", "Real-Time Multilingual Chat Backend with Recipient-Centric Translation",
                "endpoints", Map.of(
                        "health", "/api/health",
                        "direct_health", "/health",
                        "actuator_health", "/actuator/health",
                        "actuator_metrics", "/actuator/metrics",
                        "languages", "/api/languages",
                        "websocket", "/ws"
                )
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> directHealth() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "application", "TalkTranslate",
                "timestamp", System.currentTimeMillis()
        ));
    }
}
