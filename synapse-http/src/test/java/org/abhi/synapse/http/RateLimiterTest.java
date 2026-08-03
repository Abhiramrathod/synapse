package org.abhi.synapse.http;

import org.abhi.synapse.core.exception.SynapseException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RateLimiterTest {

    @Test
    void admitsUpToLimitThenRejects() {
        RateLimiter limiter = new RateLimiter(3, Duration.ofMinutes(1));
        assertThat(limiter.tryAcquire()).isTrue();
        assertThat(limiter.tryAcquire()).isTrue();
        assertThat(limiter.tryAcquire()).isTrue();
        assertThat(limiter.tryAcquire()).isFalse();
    }

    @Test
    void acquireThrowsRateLimitErrorWhenExhausted() {
        RateLimiter limiter = new RateLimiter(1, Duration.ofMinutes(1));
        limiter.tryAcquire();
        assertThatThrownBy(limiter::acquire)
                .isInstanceOf(SynapseException.class)
                .matches(e -> ((SynapseException) e).getType()
                        == SynapseException.ExceptionType.RATE_LIMIT_ERROR);
    }

    @Test
    void windowSlidesAndAdmitsAgain() throws Exception {
        RateLimiter limiter = new RateLimiter(1, Duration.ofMillis(50));
        assertThat(limiter.tryAcquire()).isTrue();
        assertThat(limiter.tryAcquire()).isFalse();
        Thread.sleep(80);
        assertThat(limiter.tryAcquire()).isTrue();
    }

    @Test
    void rejectsInvalidConstructorArgs() {
        assertThatThrownBy(() -> new RateLimiter(0, Duration.ofMinutes(1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RateLimiter(2, Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
