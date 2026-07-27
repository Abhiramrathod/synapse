package org.abhi.synapse.http;

import org.abhi.synapse.core.exception.SynapseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CircuitBreakerTest {

    private CircuitBreaker breaker;

    @BeforeEach
    void setUp() {
        breaker = new CircuitBreaker(3, Duration.ofSeconds(1));
    }

    @Test
    void startsInClosedState() {
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void allowsRequestsWhenClosed() throws SynapseException {
        breaker.allowRequest();
    }

    @Test
    void opensAfterFailureThreshold() {
        breaker.recordFailure();
        breaker.recordFailure();
        breaker.recordFailure();

        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    void openCircuitBreakerRejectsRequests() {
        breaker.recordFailure();
        breaker.recordFailure();
        breaker.recordFailure();

        assertThatThrownBy(() -> breaker.allowRequest())
                .isInstanceOf(SynapseException.class)
                .hasMessageContaining("Circuit breaker is open");
    }

    @Test
    void successResetsFailureCount() {
        breaker.recordFailure();
        breaker.recordFailure();
        breaker.recordSuccess();

        assertThat(breaker.getFailureCount()).isEqualTo(0);
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void transitionsToHalfOpenAfterOpenDuration() throws Exception {
        breaker.recordFailure();
        breaker.recordFailure();
        breaker.recordFailure();
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        Thread.sleep(1100);

        breaker.allowRequest();
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.HALF_OPEN);
    }

    @Test
    void halfOpenSuccessCloses() throws Exception {
        breaker.recordFailure();
        breaker.recordFailure();
        breaker.recordFailure();
        Thread.sleep(1100);
        breaker.allowRequest();
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.HALF_OPEN);

        breaker.recordSuccess();
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void halfOpenFailureReopens() throws Exception {
        breaker.recordFailure();
        breaker.recordFailure();
        breaker.recordFailure();
        Thread.sleep(1100);
        breaker.allowRequest();
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.HALF_OPEN);

        breaker.recordFailure();
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }
}
