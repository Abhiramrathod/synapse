package org.abhi.synapse.core.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SynapseMetricsSummaryTest {

    @Test
    void constructorWithAllFields() {
        SynapseMetricsSummary summary = new SynapseMetricsSummary(
                "gpt-4", "openai", 1250L, 150, 320, true
        );

        assertThat(summary.getModel()).isEqualTo("gpt-4");
        assertThat(summary.getProvider()).isEqualTo("openai");
        assertThat(summary.getLatencyMs()).isEqualTo(1250L);
        assertThat(summary.getPromptTokens()).isEqualTo(150);
        assertThat(summary.getCompletionTokens()).isEqualTo(320);
        assertThat(summary.getTotalTokens()).isEqualTo(470);
        assertThat(summary.isSuccess()).isTrue();
    }

    @Test
    void backwardCompatibleConstructor() {
        SynapseMetricsSummary summary = new SynapseMetricsSummary(
                "gpt-4", 500L, 100, 200, false
        );

        assertThat(summary.getModel()).isEqualTo("gpt-4");
        assertThat(summary.getProvider()).isNull();
        assertThat(summary.getLatencyMs()).isEqualTo(500L);
        assertThat(summary.isSuccess()).isFalse();
    }
}
