package org.abhi.synapse.http;

import org.abhi.synapse.core.exception.SynapseException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class CircuitBreakerConcurrencyTest {

    @Test
    void onlySingleProbeAllowedInHalfOpen() throws Exception {
        CircuitBreaker breaker = new CircuitBreaker(2, Duration.ofMillis(50));
        breaker.recordFailure();
        breaker.recordFailure();
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        Thread.sleep(100);

        int threads = 32;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger allowed = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    breaker.allowRequest();
                    allowed.incrementAndGet();
                } catch (SynapseException e) {
                    rejected.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
        pool.shutdownNow();

        assertThat(allowed.get()).isEqualTo(1);
        assertThat(rejected.get()).isEqualTo(threads - 1);
    }

    @Test
    void successfulProbeClosesCircuit() throws Exception {
        CircuitBreaker breaker = new CircuitBreaker(1, Duration.ofMillis(50));
        breaker.recordFailure();
        Thread.sleep(100);
        breaker.allowRequest();
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.HALF_OPEN);
        breaker.recordSuccess();
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        breaker.allowRequest();
    }

    @Test
    void failedProbeReopensCircuit() throws Exception {
        CircuitBreaker breaker = new CircuitBreaker(1, Duration.ofMillis(50));
        breaker.recordFailure();
        Thread.sleep(100);
        breaker.allowRequest();
        breaker.recordFailure();
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }
}
