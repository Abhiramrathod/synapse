package org.abhi.synapse.http;

import org.abhi.synapse.core.exception.SynapseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CircuitBreaker {
    private static final Logger log = LoggerFactory.getLogger(CircuitBreaker.class);
    public enum State { CLOSED, OPEN, HALF_OPEN }
    private final int failureThreshold;
    private final Duration openDuration;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private volatile Instant openedAt;

    public CircuitBreaker(int failureThreshold, Duration openDuration) {
        this.failureThreshold = failureThreshold; this.openDuration = openDuration;
    }
    public void recordSuccess() {
        failureCount.set(0);
        if (state.get() == State.HALF_OPEN) { state.set(State.CLOSED); log.debug("[Synapse] Circuit breaker closed"); }
    }
    public void recordFailure() {
        if (state.get() == State.HALF_OPEN) { trip(); return; }
        if (failureCount.incrementAndGet() >= failureThreshold) trip();
    }
    private void trip() {
        state.set(State.OPEN); openedAt = Instant.now();
        log.warn("[Synapse] Circuit breaker opened after {} failures", failureCount.get());
    }
    public void allowRequest() throws SynapseException {
        State current = state.get();
        if (current == State.CLOSED) return;
        if (current == State.OPEN) {
            if (Instant.now().isAfter(openedAt.plus(openDuration))) {
                state.set(State.HALF_OPEN); log.debug("[Synapse] Circuit breaker half-open"); return;
            }
            throw new SynapseException("Circuit breaker is open", SynapseException.ExceptionType.CIRCUIT_BREAKER_OPEN);
        }
    }
    public State getState() { return state.get(); }
    public int getFailureCount() { return failureCount.get(); }
}
