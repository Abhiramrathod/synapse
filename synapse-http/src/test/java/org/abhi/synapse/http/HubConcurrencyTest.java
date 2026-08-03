package org.abhi.synapse.http;

import org.abhi.synapse.config.SynapseConfig;
import org.abhi.synapse.core.model.SynapseResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class HubConcurrencyTest {

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void handlesOneHundredConcurrentRequests() throws Exception {
        int calls = 100;
        String okBody = "{\"id\":\"cmpl-c\",\"object\":\"chat.completion\",\"model\":\"gpt-4\","
                + "\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"ok\"},"
                + "\"finish_reason\":\"stop\"}]}";
        for (int i = 0; i < calls; i++) {
            mockWebServer.enqueue(new MockResponse()
                    .setBody(okBody)
                    .addHeader("Content-Type", "application/json"));
        }

        SynapseConfig config = SynapseConfig.builder()
                .baseUrl(mockWebServer.url("/").toString().replaceAll("/$", ""))
                .endpoint("/v1/chat/completions")
                .apiKey("sk-test-key")
                .modelName("gpt-4")
                .maxRetries(0)
                .maxConcurrentRequests(64)
                .build();

        try (SynapseHub hub = new SynapseHub(config)) {
            ExecutorService pool = Executors.newFixedThreadPool(16);
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(calls);
            AtomicInteger ok = new AtomicInteger();
            AtomicInteger failed = new AtomicInteger();

            for (int i = 0; i < calls; i++) {
                pool.submit(() -> {
                    try {
                        start.await();
                        SynapseResponse response = hub.sendPrompt("hi", null);
                        if ("ok".equals(response.getContent())) {
                            ok.incrementAndGet();
                        } else {
                            failed.incrementAndGet();
                        }
                    } catch (Exception e) {
                        failed.incrementAndGet();
                    } finally {
                        done.countDown();
                    }
                });
            }
            start.countDown();
            assertThat(done.await(20, TimeUnit.SECONDS)).isTrue();
            pool.shutdownNow();

            assertThat(failed.get()).isZero();
            assertThat(ok.get()).isEqualTo(calls);
        }
    }

    @Test
    void rateLimitRejectsRequestsBeyondPerMinuteCap() throws Exception {
        String okBody = "{\"id\":\"cmpl-r\",\"object\":\"chat.completion\",\"model\":\"gpt-4\","
                + "\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"ok\"},"
                + "\"finish_reason\":\"stop\"}]}";
        for (int i = 0; i < 5; i++) {
            mockWebServer.enqueue(new MockResponse()
                    .setBody(okBody)
                    .addHeader("Content-Type", "application/json"));
        }

        SynapseConfig config = SynapseConfig.builder()
                .baseUrl(mockWebServer.url("/").toString().replaceAll("/$", ""))
                .endpoint("/v1/chat/completions")
                .apiKey("sk-test-key")
                .modelName("gpt-4")
                .maxRetries(0)
                .maxRequestsPerMinute(2)
                .build();

        try (SynapseHub hub = new SynapseHub(config)) {
            hub.sendPrompt("one", null);
            hub.sendPrompt("two", null);
            org.assertj.core.api.Assertions.assertThatThrownBy(() -> hub.sendPrompt("three", null))
                    .isInstanceOf(org.abhi.synapse.core.exception.SynapseException.class)
                    .satisfies(e -> org.assertj.core.api.Assertions.assertThat(
                            ((org.abhi.synapse.core.exception.SynapseException) e).getType())
                            .isEqualTo(org.abhi.synapse.core.exception.SynapseException.ExceptionType.RATE_LIMIT_ERROR));
        }
    }
}
