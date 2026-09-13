package com.talktranslate;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class TalkTranslateApplicationTests {

    @Test
    void shouldHaveSpringBootApplicationAnnotation() {
        assertThat(TalkTranslateApplication.class.isAnnotationPresent(SpringBootApplication.class)).isTrue();
    }

    @Test
    void shouldInstantiateApplicationClass() {
        TalkTranslateApplication application = new TalkTranslateApplication();
        assertThat(application).isNotNull();
    }

    @Test
    void shouldExecuteMainMethodSuccessfully() {
        try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
            ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);
            mockedSpringApplication.when(() -> SpringApplication.run(eq(TalkTranslateApplication.class), any(String[].class)))
                    .thenReturn(mockContext);

            String[] args = new String[]{"--server.port=8080"};
            assertDoesNotThrow(() -> TalkTranslateApplication.main(args));

            mockedSpringApplication.verify(() -> SpringApplication.run(eq(TalkTranslateApplication.class), eq(args)));
        }
    }

    @Test
    void shouldExecuteMainMethodWithEmptyArgs() {
        try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
            ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);
            mockedSpringApplication.when(() -> SpringApplication.run(eq(TalkTranslateApplication.class), any(String[].class)))
                    .thenReturn(mockContext);

            String[] emptyArgs = new String[]{};
            assertDoesNotThrow(() -> TalkTranslateApplication.main(emptyArgs));

            mockedSpringApplication.verify(() -> SpringApplication.run(eq(TalkTranslateApplication.class), eq(emptyArgs)));
        }
    }

    @Test
    void shouldExecuteMainMethodWithNullArgs() {
        try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
            mockedSpringApplication.when(() -> SpringApplication.run(eq(TalkTranslateApplication.class), (String[]) any()))
                    .thenReturn(mock(ConfigurableApplicationContext.class));

            assertDoesNotThrow(() -> TalkTranslateApplication.main(null));

            mockedSpringApplication.verify(() -> SpringApplication.run(eq(TalkTranslateApplication.class), (String[]) any()));
        }
    }

    @Test
    void shouldHaveMainMethodWithExpectedSignature() throws NoSuchMethodException {
        Method mainMethod = TalkTranslateApplication.class.getMethod("main", String[].class);
        assertThat(mainMethod).isNotNull();
        assertThat(mainMethod.getReturnType()).isEqualTo(void.class);
    }
}
