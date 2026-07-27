package org.abhi.synapse.http;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

public class ConcurrencyLimiter {
    private final Semaphore semaphore;
    private final AtomicLong totalAcquired = new AtomicLong(0);
    public ConcurrencyLimiter(int maxConcurrent) { this.semaphore = new Semaphore(maxConcurrent, true); }
    public void acquire() throws InterruptedException { semaphore.acquire(); totalAcquired.incrementAndGet(); }
    public void release() { semaphore.release(); }
    public boolean tryAcquire() { boolean a = semaphore.tryAcquire(); if (a) totalAcquired.incrementAndGet(); return a; }
    public int availablePermits() { return semaphore.availablePermits(); }
    public long getTotalAcquired() { return totalAcquired.get(); }
}
