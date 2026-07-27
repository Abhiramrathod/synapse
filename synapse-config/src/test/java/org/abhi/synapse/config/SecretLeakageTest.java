package org.abhi.synapse.config;

import org.abhi.synapse.core.model.SynapseRequestContext;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SecretLeakageTest {

    @Test
    void requestContextToStringMasksApiKey() {
        Map<String, String> headers = Map.of(
                "Content-Type", "application/json",
                "Authorization", "Bearer sk-secret-key-12345"
        );
        SynapseRequestContext ctx = new SynapseRequestContext(
                "https://api.openai.com/v1/chat/completions",
                "{\"model\":\"gpt-4\"}",
                headers,
                false,
                "gpt-4"
        );

        String str = ctx.toString();
        assertThat(str).doesNotContain("sk-secret-key-12345");
        assertThat(str).contains("REDACTED");
    }

    @Test
    void requestContextToStringRedactsHeadersContainingKey() {
        Map<String, String> headers = Map.of(
                "Content-Type", "application/json",
                "X-API-KEY", "secret-value"
        );
        SynapseRequestContext ctx = new SynapseRequestContext(
                "https://api.openai.com/v1/chat/completions",
                "{\"model\":\"gpt-4\"}",
                headers,
                false,
                "gpt-4"
        );

        String str = ctx.toString();
        assertThat(str).doesNotContain("secret-value");
    }

    @Test
    void synapseConfigToStringRedactsApiKey() {
        SynapseConfig config = SynapseConfig.builder()
                .baseUrl("https://api.openai.com")
                .endpoint("/v1/chat/completions")
                .apiKey("sk-top-secret-99999")
                .modelName("gpt-4")
                .build();

        assertThat(config.toString()).doesNotContain("sk-top-secret-99999");
        assertThat(config.toString()).contains("***REDACTED***");
    }
}
