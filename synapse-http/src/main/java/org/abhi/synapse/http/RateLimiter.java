package org.abhi.synapse.http;

import org.abhi.synapse.core.exception.SynapseException;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Sliding-window rate limiter that caps the number of admitted requests within
 * a rolling time window.
 *
 * <p>Admission is checked against {@link #tryAcquire()} which is safe for use
 * from concurrent threads. When the limit is exceeded,
 * {@link #acquire()} throws a {@link SynapseException} of type
 * {@link SynapseException.ExceptionType#RATE_LIMIT_ERROR}.</p>
 */
public class RateLimiter {

    private final int maxRequests;
    private final long windowNanos;
    private final Deque<Long> timestamps = new ArrayDeque<>();
    private final ReentrantLock lock = new ReentrantLock();

    public RateLimiter(int maxRequests, Duration window) {
        if (maxRequests <= 0) throw new IllegalArgumentException("maxRequests must be positive");
        if (window == null || window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("window must be a positive duration");
        }
        this.maxRequests = maxRequests;
        this.windowNanos = window.toNanos();
    }

    /**
     * Attempts to acquire a permit within the current sliding window.
     *
     * @return {@code true} if the request is admitted, {@code false} if the
     *         window limit has been reached
     */
    public boolean tryAcquire() {
        lock.lock();
        try {
            long now = System.nanoTime();
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() >= windowNanos) {
                timestamps.removeFirst();
            }
            if (timestamps.size() >= maxRequests) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Acquires a permit, throwing if the sliding window limit has been reached.
     *
     * @throws SynapseException with type {@link SynapseException.ExceptionType#RATE_LIMIT_ERROR}
     *         when the window limit is exceeded
     */
    public void acquire() throws SynapseException {
        if (!tryAcquire()) {
            throw new SynapseException("Rate limit exceeded: max " + maxRequests
                    + " requests per window", SynapseException.ExceptionType.RATE_LIMIT_ERROR);
        }
    }
}
