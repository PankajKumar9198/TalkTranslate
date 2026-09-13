package com.talktranslate.service;

import com.talktranslate.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class JwtTokenServiceTest {

    private static final String DEFAULT_SECRET = "TestSecretKey12345678901234567890";
    private static final long DEFAULT_EXPIRATION_MS = 3600000; // 1 hour

    private JwtTokenService jwtTokenService;
    private User testUser;
    private User secondUser;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService(DEFAULT_SECRET, DEFAULT_EXPIRATION_MS);

        testUser = User.builder()
                .id("usr_123")
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .preferredLanguage("en")
                .build();

        secondUser = User.builder()
                .id("usr_456")
                .username("second_user")
                .email("second@example.com")
                .fullName("Second User")
                .preferredLanguage("es")
                .build();
    }

    @Test
    void shouldGenerateValidThreePartToken() {
        String token = jwtTokenService.generateToken(testUser);

        assertThat(token).isNotNull().isNotBlank();
        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);
    }

    @Test
    void shouldContainCorrectHeaderAndPayloadClaims() {
        String token = jwtTokenService.generateToken(testUser);
        String[] parts = token.split("\\.");

        String decodedHeader = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        String decodedPayload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

        assertAll(
                () -> assertThat(decodedHeader).contains("\"alg\":\"HS256\"").contains("\"typ\":\"JWT\""),
                () -> assertThat(decodedPayload).contains("\"sub\":\"testuser\""),
                () -> assertThat(decodedPayload).contains("\"uid\":\"usr_123\""),
                () -> assertThat(decodedPayload).contains("\"iat\":"),
                () -> assertThat(decodedPayload).contains("\"exp\":"));
    }

    @Test
    void shouldGenerateTokenForUserWithSpecialCharacters() {
        User unicodeUser = User.builder()
                .id("usr_ç@rlos-123_#")
                .username("carlos_garcía_99")
                .email("carlos@example.com")
                .fullName("Carlos García")
                .build();

        String token = jwtTokenService.generateToken(unicodeUser);

        assertThat(token).isNotNull();
        assertThat(jwtTokenService.validateToken(token)).isTrue();
        assertThat(jwtTokenService.extractUsername(token)).isEqualTo("carlos_garcía_99");
        assertThat(jwtTokenService.extractUserId(token)).isEqualTo("usr_ç@rlos-123_#");
    }

    @Test
    void shouldGenerateDistinctTokensForDifferentUsers() {
        String token1 = jwtTokenService.generateToken(testUser);
        String token2 = jwtTokenService.generateToken(secondUser);

        assertThat(token1).isNotEqualTo(token2);
        assertThat(jwtTokenService.extractUserId(token1)).isEqualTo("usr_123");
        assertThat(jwtTokenService.extractUserId(token2)).isEqualTo("usr_456");
    }

    @Test
    void shouldValidateFreshlyGeneratedToken() {
        String token = jwtTokenService.generateToken(testUser);
        assertThat(jwtTokenService.validateToken(token)).isTrue();
    }

    @Test
    void shouldValidateTokenWithBearerPrefix() {
        String token = jwtTokenService.generateToken(testUser);
        assertThat(jwtTokenService.validateToken("Bearer " + token)).isTrue();
    }

    @Test
    void shouldValidateTokenWithExtraSpacesInBearerPrefix() {
        String token = jwtTokenService.generateToken(testUser);
        assertThat(jwtTokenService.validateToken("Bearer    " + token)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   ", "\t", "\n" })
    void shouldRejectNullEmptyOrBlankTokens(String invalidToken) {
        assertThat(jwtTokenService.validateToken(invalidToken)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalidTokenWithoutDots",
            "partOne.partTwo",
            "partOne.partTwo.partThree.partFour",
            "Bearer ",
            "Bearer",
            "..",
            ".payload.signature",
            "header..signature",
            "header.payload."
    })
    void shouldRejectMalformedTokens(String malformedToken) {
        assertThat(jwtTokenService.validateToken(malformedToken)).isFalse();
    }

    @Test
    void shouldRejectTamperedHeader() {
        String token = jwtTokenService.generateToken(testUser);
        String[] parts = token.split("\\.");

        String tamperedHeader = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        String tamperedToken = tamperedHeader + "." + parts[1] + "." + parts[2];

        assertThat(jwtTokenService.validateToken(tamperedToken)).isFalse();
    }

    @Test
    void shouldRejectTamperedPayload() {
        String token = jwtTokenService.generateToken(testUser);
        String[] parts = token.split("\\.");

        String tamperedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"hacked_admin\",\"uid\":\"admin_001\"}".getBytes(StandardCharsets.UTF_8));
        String tamperedToken = parts[0] + "." + tamperedPayload + "." + parts[2];

        assertThat(jwtTokenService.validateToken(tamperedToken)).isFalse();
    }

    @Test
    void shouldRejectTamperedSignature() {
        String token = jwtTokenService.generateToken(testUser);
        String[] parts = token.split("\\.");

        String tamperedToken = parts[0] + "." + parts[1] + ".tampered_fake_signature_abc123";

        assertThat(jwtTokenService.validateToken(tamperedToken)).isFalse();
    }

    @Test
    void shouldRejectTokenSignedWithDifferentSecretKey() {
        JwtTokenService anotherService = new JwtTokenService("AnotherDifferentSecretKey9876543210",
                DEFAULT_EXPIRATION_MS);
        String tokenFromAnotherService = anotherService.generateToken(testUser);

        assertThat(anotherService.validateToken(tokenFromAnotherService)).isTrue();
        assertThat(jwtTokenService.validateToken(tokenFromAnotherService)).isFalse();
    }

    @Test
    void shouldExtractUsernameFromRawAndBearerToken() {
        String token = jwtTokenService.generateToken(testUser);

        assertThat(jwtTokenService.extractUsername(token)).isEqualTo("testuser");
        assertThat(jwtTokenService.extractUsername("Bearer " + token)).isEqualTo("testuser");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   ", "Bearer ", "untracked.token.value", "fake_token" })
    void shouldReturnNullWhenExtractingUsernameFromInvalidToken(String invalidToken) {
        assertThat(jwtTokenService.extractUsername(invalidToken)).isNull();
    }

    @Test
    void shouldExtractUserIdFromRawAndBearerToken() {
        String token = jwtTokenService.generateToken(testUser);

        assertThat(jwtTokenService.extractUserId(token)).isEqualTo("usr_123");
        assertThat(jwtTokenService.extractUserId("Bearer " + token)).isEqualTo("usr_123");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   ", "Bearer ", "untracked.token.value", "fake_token" })
    void shouldReturnNullWhenExtractingUserIdFromInvalidToken(String invalidToken) {
        assertThat(jwtTokenService.extractUserId(invalidToken)).isNull();
    }

    @Test
    void shouldIsolateClaimsForMultipleUsers() {
        String tokenUser1 = jwtTokenService.generateToken(testUser);
        String tokenUser2 = jwtTokenService.generateToken(secondUser);

        assertAll(
                () -> assertThat(jwtTokenService.extractUsername(tokenUser1)).isEqualTo("testuser"),
                () -> assertThat(jwtTokenService.extractUserId(tokenUser1)).isEqualTo("usr_123"),
                () -> assertThat(jwtTokenService.extractUsername(tokenUser2)).isEqualTo("second_user"),
                () -> assertThat(jwtTokenService.extractUserId(tokenUser2)).isEqualTo("usr_456"));
    }

    @Test
    void shouldReturnFalseForFreshToken() {
        String token = jwtTokenService.generateToken(testUser);

        assertThat(jwtTokenService.isTokenExpired(token)).isFalse();
        assertThat(jwtTokenService.isTokenExpired("Bearer " + token)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   ", "Bearer ", "untracked.token.value", "random_string" })
    void shouldReturnTrueForNullOrUntrackedToken(String invalidToken) {
        assertThat(jwtTokenService.isTokenExpired(invalidToken)).isTrue();
    }

    @Test
    void shouldDetectExpiredTokenAfterExpiration() throws InterruptedException {
        JwtTokenService shortLivedService = new JwtTokenService("TestSecretKeyShortLived123", 20);
        String token = shortLivedService.generateToken(testUser);

        assertThat(shortLivedService.isTokenExpired(token)).isFalse();

        Thread.sleep(40);

        assertThat(shortLivedService.isTokenExpired(token)).isTrue();
        assertThat(shortLivedService.validateToken(token)).isFalse();
    }

    @Test
    void shouldEvictExpiredTokenFromStoreDuringValidation() throws InterruptedException {
        JwtTokenService shortLivedService = new JwtTokenService("TestSecretKeyShortLived123", 20);
        String token = shortLivedService.generateToken(testUser);

        Thread.sleep(40);

        boolean isValid = shortLivedService.validateToken(token);
        assertThat(isValid).isFalse();

        assertThat(shortLivedService.extractUsername(token)).isNull();
        assertThat(shortLivedService.extractUserId(token)).isNull();
    }

    @Test
    void shouldInstantiateTokenMetadataRecord() {
        long now = System.currentTimeMillis();
        JwtTokenService.TokenMetadata meta = new JwtTokenService.TokenMetadata("usr_123", "testuser", now + 60000);

        assertAll(
                () -> assertThat(meta.userId()).isEqualTo("usr_123"),
                () -> assertThat(meta.username()).isEqualTo("testuser"),
                () -> assertThat(meta.expiresAt()).isEqualTo(now + 60000));
    }

    @Test
    void shouldSupportCustomConstructorConfig() {
        JwtTokenService customService = new JwtTokenService("CustomKey999888777", 7200000);
        String token = customService.generateToken(testUser);

        assertThat(token).isNotNull();
        assertThat(customService.validateToken(token)).isTrue();
        assertThat(customService.extractUsername(token)).isEqualTo("testuser");
    }
}
