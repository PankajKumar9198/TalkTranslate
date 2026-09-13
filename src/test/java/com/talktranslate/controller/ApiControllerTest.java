package com.talktranslate.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talktranslate.model.SupportedLanguage;
import com.talktranslate.model.TranslationRequest;
import com.talktranslate.model.TranslationResponse;
import com.talktranslate.service.TranslationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApiController.class)
class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TranslationService translationService;

    @Test
    void shouldReturnHealthStatus() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<Map<String, Object>>() {}
        );

        assertThat(response).isNotNull();
        assertThat(response.get("status")).isEqualTo("UP");
        assertThat(response.get("application")).isEqualTo("TalkTranslate");
        assertThat(response.get("timestamp")).isNotNull();
    }

    @Test
    void shouldGetSupportedLanguages() throws Exception {
        List<SupportedLanguage> languages = List.of(
                new SupportedLanguage("en", "English", "English", "🇺🇸", "en-US"),
                new SupportedLanguage("hi", "Hindi", "हिन्दी", "🇮🇳", "hi-IN")
        );

        when(translationService.getSupportedLanguages()).thenReturn(languages);

        MvcResult result = mockMvc.perform(get("/api/languages"))
                .andExpect(status().isOk())
                .andReturn();

        List<SupportedLanguage> actual = objectMapper.readValue(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<List<SupportedLanguage>>() {}
        );

        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).getCode()).isEqualTo("en");
        assertThat(actual.get(1).getCode()).isEqualTo("hi");
        verify(translationService, times(1)).getSupportedLanguages();
    }

    @Test
    void shouldReturnEmptyListWhenNoLanguages() throws Exception {
        when(translationService.getSupportedLanguages()).thenReturn(Collections.emptyList());

        MvcResult result = mockMvc.perform(get("/api/languages"))
                .andExpect(status().isOk())
                .andReturn();

        List<SupportedLanguage> actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<List<SupportedLanguage>>() {}
        );

        assertThat(actual).isEmpty();
        verify(translationService, times(1)).getSupportedLanguages();
    }

    @Test
    void shouldTranslateText() throws Exception {
        TranslationRequest request = new TranslationRequest("Hello", "en", "hi");
        TranslationResponse response = new TranslationResponse(
                "Hello",
                "नमस्ते",
                "en",
                "hi",
                "en",
                true
        );

        when(translationService.translate("Hello", "en", "hi")).thenReturn(response);

        MvcResult result = mockMvc.perform(post("/api/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        TranslationResponse actual = objectMapper.readValue(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                TranslationResponse.class
        );

        assertThat(actual).isNotNull();
        assertThat(actual.getOriginalText()).isEqualTo("Hello");
        assertThat(actual.getTranslatedText()).isEqualTo("नमस्ते");
        assertThat(actual.getSourceLang()).isEqualTo("en");
        assertThat(actual.getTargetLang()).isEqualTo("hi");
        verify(translationService, times(1)).translate("Hello", "en", "hi");
    }

    @Test
    void shouldTranslateWithAutoLanguageDetection() throws Exception {
        TranslationRequest request = new TranslationRequest("नमस्ते", "auto", "en");
        TranslationResponse response = new TranslationResponse("नमस्ते", "Hello", "auto", "en", "hi", false);

        when(translationService.translate("नमस्ते", "auto", "en")).thenReturn(response);

        MvcResult result = mockMvc.perform(post("/api/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        TranslationResponse actual = objectMapper.readValue(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                TranslationResponse.class
        );

        assertThat(actual.getTranslatedText()).isEqualTo("Hello");
        assertThat(actual.getDetectedLang()).isEqualTo("hi");
        verify(translationService, times(1)).translate("नमस्ते", "auto", "en");
    }

    @Test
    void shouldTranslateWithEmptyPayload() throws Exception {
        TranslationResponse empty = new TranslationResponse("", "", null, null, null, false);
        when(translationService.translate(null, null, null)).thenReturn(empty);

        MvcResult result = mockMvc.perform(post("/api/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andReturn();

        TranslationResponse actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                TranslationResponse.class
        );

        assertThat(actual.getOriginalText()).isEmpty();
        assertThat(actual.getTranslatedText()).isEmpty();
        verify(translationService, times(1)).translate(null, null, null);
    }

    @Test
    void shouldHandleMalformedJson() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid_json_format:"))
                .andExpect(status().isInternalServerError())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).contains("JSON parse error");
        verify(translationService, never()).translate(any(), any(), any());
    }

    @Test
    void shouldHandleServiceException() throws Exception {
        TranslationRequest request = new TranslationRequest("Test", "en", "fr");
        when(translationService.translate("Test", "en", "fr"))
                .thenThrow(new RuntimeException("Translation engine unavailable"));

        MvcResult result = mockMvc.perform(post("/api/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).contains("Translation engine unavailable");
    }
}
