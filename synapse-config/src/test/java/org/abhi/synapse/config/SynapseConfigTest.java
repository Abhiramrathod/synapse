package org.abhi.synapse.config;

import org.abhi.synapse.core.exception.SynapseException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SynapseConfigTest {

    @Test
    void defaultsAreSet() {
        SynapseConfig config = SynapseConfig.builder()
                .baseUrl("https://api.openai.com")
                .endpoint("/v1/chat/completions")
                .apiKey("sk-test")
                .modelName("gpt-4")
                .build();

        assertThat(config.getTemperature()).isEqualTo(0.7);
        assertThat(config.getMaxTokens()).isEqualTo(1024);
        assertThat(config.getMaxRetries()).isEqualTo(3);
        assertThat(config.isEnableLogging()).isTrue();
    }

    @Test
    void customValuesArePreserved() {
        SynapseConfig config = SynapseConfig.builder()
                .baseUrl("https://api.anthropic.com")
                .endpoint("/v1/messages")
                .apiKey("sk-ant-test")
                .modelName("claude-3")
                .temperature(0.5)
                .maxTokens(4096)
                .maxRetries(5)
                .enableLogging(true)
                .build();

        assertThat(config.getBaseUrl()).isEqualTo("https://api.anthropic.com");
        assertThat(config.getEndpoint()).isEqualTo("/v1/messages");
        assertThat(config.getModelName()).isEqualTo("claude-3");
        assertThat(config.getTemperature()).isEqualTo(0.5);
        assertThat(config.getMaxTokens()).isEqualTo(4096);
        assertThat(config.getMaxRetries()).isEqualTo(5);
        assertThat(config.isEnableLogging()).isTrue();
    }

    @Test
    void validateThrowsOnMissingBaseUrl() {
        assertThatThrownBy(() -> SynapseConfig.builder()
                .endpoint("/v1/chat/completions")
                .apiKey("sk-test")
                .modelName("gpt-4")
                .build().validate())
                .isInstanceOf(SynapseException.class)
                .hasMessageContaining("baseUrl");
    }

    @Test
    void validateThrowsOnMissingApiKey() {
        assertThatThrownBy(() -> SynapseConfig.builder()
                .baseUrl("https://api.openai.com")
                .endpoint("/v1/chat/completions")
                .modelName("gpt-4")
                .build().validate())
                .isInstanceOf(SynapseException.class)
                .hasMessageContaining("apiKey");
    }

    @Test
    void validateThrowsOnMissingModelName() {
        assertThatThrownBy(() -> SynapseConfig.builder()
                .baseUrl("https://api.openai.com")
                .endpoint("/v1/chat/completions")
                .apiKey("sk-test")
                .build().validate())
                .isInstanceOf(SynapseException.class)
                .hasMessageContaining("modelName");
    }

    @Test
    void toStringRedactsApiKey() {
        SynapseConfig config = SynapseConfig.builder()
                .baseUrl("https://api.openai.com")
                .endpoint("/v1/chat/completions")
                .apiKey("sk-secret-key-12345")
                .modelName("gpt-4")
                .build();

        assertThat(config.toString()).contains("***REDACTED***");
        assertThat(config.toString()).doesNotContain("sk-secret-key-12345");
    }

    @Test
    void configIsImmutable() {
        SynapseConfig config = SynapseConfig.builder()
                .baseUrl("https://api.openai.com")
                .endpoint("/v1/chat/completions")
                .apiKey("sk-test")
                .modelName("gpt-4")
                .build();

        assertThat(config.getBaseUrl()).isNotNull();
        assertThat(config.getEndpoint()).isNotNull();
        assertThat(config.getApiKey()).isNotNull();
        assertThat(config.getModelName()).isNotNull();
    }

    @Test
    void splitTimeoutsAreSet() {
        SynapseConfig config = SynapseConfig.builder()
                .baseUrl("https://api.openai.com")
                .endpoint("/v1/chat/completions")
                .apiKey("sk-test")
                .modelName("gpt-4")
                .connectTimeout(java.time.Duration.ofSeconds(5))
                .readTimeout(java.time.Duration.ofSeconds(30))
                .requestTimeout(java.time.Duration.ofSeconds(60))
                .streamIdleTimeout(java.time.Duration.ofSeconds(10))
                .build();

        assertThat(config.getConnectTimeout()).isEqualTo(java.time.Duration.ofSeconds(5));
        assertThat(config.getReadTimeout()).isEqualTo(java.time.Duration.ofSeconds(30));
        assertThat(config.getRequestTimeout()).isEqualTo(java.time.Duration.ofSeconds(60));
        assertThat(config.getStreamIdleTimeout()).isEqualTo(java.time.Duration.ofSeconds(10));
    }

    @Test
    void circuitBreakerDefaults() {
        SynapseConfig config = SynapseConfig.builder()
                .baseUrl("https://api.openai.com")
                .endpoint("/v1/chat/completions")
                .apiKey("sk-test")
                .modelName("gpt-4")
                .build();

        assertThat(config.getMaxConcurrentRequests()).isEqualTo(64);
        assertThat(config.getCircuitBreakerFailureThreshold()).isEqualTo(5);
        assertThat(config.getCircuitBreakerOpenDuration()).isEqualTo(java.time.Duration.ofSeconds(30));
    }
}
