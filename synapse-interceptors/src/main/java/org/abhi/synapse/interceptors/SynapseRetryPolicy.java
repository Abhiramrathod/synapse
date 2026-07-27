package org.abhi.synapse.interceptors;

import org.abhi.synapse.core.exception.SynapseException;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public interface SynapseRetryPolicy {

    default boolean shouldRetry(int attempt, SynapseException error) {
        return attempt < getMaxRetries();
    }

    default long getDelay(int attempt, SynapseException exception, Map<String, List<String>> responseHeaders) {
        if (responseHeaders != null) {
            String retryAfter = firstHeader(responseHeaders, "Retry-After");
            if (retryAfter != null) {
                long parsed = parseRetryAfter(retryAfter);
                if (parsed >= 0) return parsed;
            }
        }
        long baseDelay = getRetryDelay().toMillis();
        long exponential = baseDelay * (1L << Math.min(attempt, 30));
        long jitter = ThreadLocalRandom.current().nextLong(0, Math.max(exponential / 2, 1));
        return Math.min(exponential + jitter, 30_000L);
    }

    default int getMaxRetries() { return 3; }

    default Duration getRetryDelay() { return Duration.ofMillis(500); }

    default Duration getMaxRetryElapsedTime() { return Duration.ofSeconds(120); }

    private static String firstHeader(Map<String, List<String>> headers, String name) {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name) && entry.getValue() != null && !entry.getValue().isEmpty()) {
                return entry.getValue().get(0);
            }
        }
        return null;
    }

    private static long parseRetryAfter(String value) {
        try {
            long seconds = Long.parseLong(value.trim());
            return Math.max(seconds * 1000L, 0);
        } catch (NumberFormatException ignored) {}
        try {
            Instant instant = Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(value.trim()));
            long millis = instant.toEpochMilli() - System.currentTimeMillis();
            return Math.max(millis, 0);
        } catch (DateTimeParseException ignored) {}
        return -1;
    }
}
