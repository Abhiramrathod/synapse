package org.abhi.synapse.http;

import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.SynapseResponse;
import org.abhi.synapse.config.SynapseConfig;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SynapseRetryHandlerTest {

    @Test
    void retriesOnRetryableException() {
        SynapseConfig config = SynapseConfig.builder()
                .baseUrl("https://api.openai.com")
                .endpoint("/v1/chat/completions")
                .apiKey("sk-test")
                .modelName("gpt-4")
                .maxRetries(2)
                .retryDelay(java.time.Duration.ofMillis(10))
                .build();

        SynapseRetryHandler handler = new SynapseRetryHandler(config);
        AtomicInteger attempts = new AtomicInteger(0);

        SynapseResponse result = handler.executeWithRetry(() -> {
            int attempt = attempts.incrementAndGet();
            if (attempt < 3) {
                throw new SynapseException(500, "Server Error");
            }
            return new SynapseResponse("success", "gpt-4", 10, 5, "stop");
        });

        assertThat(result.getContent()).isEqualTo("success");
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    void doesNotRetryOnNonRetryableException() {
        SynapseConfig config = SynapseConfig.builder()
                .baseUrl("https://api.openai.com")
                .endpoint("/v1/chat/completions")
                .apiKey("sk-test")
                .modelName("gpt-4")
                .maxRetries(3)
                .retryDelay(java.time.Duration.ofMillis(10))
                .build();

        SynapseRetryHandler handler = new SynapseRetryHandler(config);
        AtomicInteger attempts = new AtomicInteger(0);

        assertThatThrownBy(() -> handler.executeWithRetry(() -> {
            attempts.incrementAndGet();
            throw new SynapseException("Config error", SynapseException.ExceptionType.CONFIG_ERROR);
        })).isInstanceOf(SynapseException.class)
          .hasMessageContaining("Config error");

        assertThat(attempts.get()).isEqualTo(1);
    }

    @Test
    void exhaustsAllRetriesThenThrows() {
        SynapseConfig config = SynapseConfig.builder()
                .baseUrl("https://api.openai.com")
                .endpoint("/v1/chat/completions")
                .apiKey("sk-test")
                .modelName("gpt-4")
                .maxRetries(2)
                .retryDelay(java.time.Duration.ofMillis(10))
                .build();

        SynapseRetryHandler handler = new SynapseRetryHandler(config);
        AtomicInteger attempts = new AtomicInteger(0);

        assertThatThrownBy(() -> handler.executeWithRetry(() -> {
            attempts.incrementAndGet();
            throw new SynapseException(500, "Server Error");
        })).isInstanceOf(SynapseException.class)
          .satisfies(e -> assertThat(((SynapseException) e).getType())
                  .isEqualTo(SynapseException.ExceptionType.RETRY_EXHAUSTED));

        assertThat(attempts.get()).isEqualTo(3);
    }
}
