package com.talktranslate.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talktranslate.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing standard RFC 7519 HMAC-SHA256 JWT creation, signing, validation, and claims extraction.
 */
@Service
public class JwtTokenService {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenService.class);
    private static final String HMAC_SHA256 = "HmacSHA256";

    private final String secretKey;
    private final long expirationTimeMs;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // In-memory token store: token -> User metadata with millisecond-precision expiry
    private final Map<String, TokenMetadata> tokenStore = new ConcurrentHashMap<>();

    public record TokenMetadata(String userId, String username, long expiresAt) {
    }

    public JwtTokenService(
            @Value("${talktranslate.jwt.secret:dev_secret_key_change_in_production_multilingual_chat_2026}") String secretKey,
            @Value("${talktranslate.jwt.expiration-ms:86400000}") long expirationTimeMs) {
        this.secretKey = secretKey;
        this.expirationTimeMs = expirationTimeMs;
    }

    /**
     * Generates a standard RFC 7519 HMAC-SHA256 authentication token for a user.
     *
     * @param user the User entity
     * @return signed JWT token string
     */
    public String generateToken(User user) {
        long now = System.currentTimeMillis();
        long expiresAt = now + expirationTimeMs;

        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        String payloadJson = String.format("{\"sub\":\"%s\",\"uid\":\"%s\",\"iat\":%d,\"exp\":%d}",
                user.getUsername(), user.getId(), now / 1000, expiresAt / 1000);
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signature = sign(header + "." + payload);

        String token = header + "." + payload + "." + signature;
        tokenStore.put(token, new TokenMetadata(user.getId(), user.getUsername(), expiresAt));
        logger.debug("Generated standard JWT token for user ID: {}, username: {}", user.getId(), user.getUsername());
        return token;
    }

    /**
     * Validates if the token is properly structured, correctly signed with HMAC-SHA256, and unexpired.
     *
     * @param token the token string
     * @return true if token is valid and unexpired; false otherwise
     */
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        String cleanedToken = token.startsWith("Bearer ") ? token.substring(7).trim() : token.trim();
        String[] parts = cleanedToken.split("\\.");
        if (parts.length != 3) {
            logger.debug("Token validation failed: Invalid segment count");
            return false;
        }

        // Verify HMAC-SHA256 signature in constant time
        String expectedSignature = sign(parts[0] + "." + parts[1]);
        if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8),
                parts[2].getBytes(StandardCharsets.UTF_8))) {
            logger.debug("Token validation failed: Signature mismatch");
            return false;
        }

        TokenMetadata meta = tokenStore.get(cleanedToken);
        if (meta != null && System.currentTimeMillis() > meta.expiresAt()) {
            logger.debug("Token expired for user: {}", meta.username());
            tokenStore.remove(cleanedToken);
            return false;
        }

        return meta != null;
    }

    /**
     * Checks if a token is expired.
     *
     * @param token the token string
     * @return true if expired or missing metadata; false otherwise
     */
    public boolean isTokenExpired(String token) {
        if (token == null || token.isBlank()) {
            return true;
        }
        String cleanedToken = token.startsWith("Bearer ") ? token.substring(7).trim() : token.trim();
        TokenMetadata meta = tokenStore.get(cleanedToken);
        if (meta == null) {
            return true;
        }
        return System.currentTimeMillis() > meta.expiresAt();
    }

    /**
     * Extracts username claim from token metadata.
     *
     * @param token the token string
     * @return username if token is tracked; null otherwise
     */
    public String extractUsername(String token) {
        if (token == null) {
            return null;
        }
        String cleanedToken = token.startsWith("Bearer ") ? token.substring(7).trim() : token.trim();
        TokenMetadata meta = tokenStore.get(cleanedToken);
        return meta != null ? meta.username() : null;
    }

    /**
     * Extracts user ID claim from token metadata.
     *
     * @param token the token string
     * @return user ID if token is tracked; null otherwise
     */
    public String extractUserId(String token) {
        if (token == null) {
            return null;
        }
        String cleanedToken = token.startsWith("Bearer ") ? token.substring(7).trim() : token.trim();
        TokenMetadata meta = tokenStore.get(cleanedToken);
        return meta != null ? meta.userId() : null;
    }

    /**
     * Extracts username directly from verified JWT payload (stateless).
     */
    public String extractUsernameFromPayload(String token) {
        if (token == null) {
            return null;
        }
        String cleanedToken = token.startsWith("Bearer ") ? token.substring(7).trim() : token.trim();
        String[] parts = cleanedToken.split("\\.");
        if (parts.length != 3) {
            return null;
        }
        String expectedSignature = sign(parts[0] + "." + parts[1]);
        if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
            return null;
        }
        JsonNode node = parsePayload(parts[1]);
        return (node != null && node.has("sub")) ? node.get("sub").asText() : null;
    }

    /**
     * Extracts user ID directly from verified JWT payload (stateless).
     */
    public String extractUserIdFromPayload(String token) {
        if (token == null) {
            return null;
        }
        String cleanedToken = token.startsWith("Bearer ") ? token.substring(7).trim() : token.trim();
        String[] parts = cleanedToken.split("\\.");
        if (parts.length != 3) {
            return null;
        }
        String expectedSignature = sign(parts[0] + "." + parts[1]);
        if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
            return null;
        }
        JsonNode node = parsePayload(parts[1]);
        return (node != null && node.has("uid")) ? node.get("uid").asText() : null;
    }

    private JsonNode parsePayload(String base64Payload) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(base64Payload);
            return objectMapper.readTree(decoded);
        } catch (Exception e) {
            logger.debug("Failed to parse JWT payload: {}", e.getMessage());
            return null;
        }
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            logger.error("Error signing JWT token data with {}: {}", HMAC_SHA256, e.getMessage(), e);
            throw new RuntimeException("Error signing token", e);
        }
    }
}
