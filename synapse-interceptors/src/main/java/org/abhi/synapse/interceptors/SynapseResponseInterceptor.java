package org.abhi.synapse.interceptors;

import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.SynapseResponseContext;

/**
 * Interceptor for the response phase of the Synapse HTTP communication lifecycle.
 *
 * <p>Implementations of this interface can hook into the response pipeline to perform actions
 * before a response is deserialized, after a response has been fully processed, or when a
 * response error occurs. This is useful for cross-cutting concerns such as response logging,
 * metrics collection, response transformation, cache updates, and error handling.</p>
 *
 * <p>All methods have default (no-op) implementations so that implementors may override only
 * the callbacks they need.</p>
 *
 * <p>Implementation example:</p>
 * <pre>{@code
 * public class ResponseCacheInterceptor implements SynapseResponseInterceptor {
 *
 *     private final CacheManager cacheManager;
 *
 *     public ResponseCacheInterceptor(CacheManager cacheManager) {
 *         this.cacheManager = cacheManager;
 *     }
 *
 *     @Override
 *     public void afterResponse(SynapseResponseContext ctx) {
 *         if (ctx.isCacheable()) {
 *             cacheManager.put(ctx.getCacheKey(), ctx.getResponseBody());
 *         }
 *     }
 * }
 * }</pre>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 * @see SynapseRequestInterceptor
 * @see SynapseResponseContext
 */
public interface SynapseResponseInterceptor {

    /**
     * Called after a response is received but before it is deserialized or processed.
     *
     * <p>Use this callback to inspect raw response metadata such as status codes
     * and headers, or to perform early validation of the response before full
     * deserialization occurs.</p>
     *
     * @param ctx the current response context providing access to response details
     *            and mutable state
     * @since 1.0.0
     */
    default void beforeResponse(SynapseResponseContext ctx) {}

    /**
     * Called after a response has been fully processed and deserialized.
     *
     * <p>Use this callback for post-processing tasks such as logging the response,
     * updating metrics, populating a cache, or applying response transformations
     * before the result is returned to the caller.</p>
     *
     * @param ctx the current response context providing access to response details
     *            and mutable state
     * @since 1.0.0
     */
    default void afterResponse(SynapseResponseContext ctx) {}

    /**
     * Called when response processing fails with an error.
     *
     * <p>Use this callback to perform error-specific handling such as logging
     * the failure, incrementing error counters, or implementing fallback logic.
     * The response pipeline will not invoke {@link #afterResponse(SynapseResponseContext)}
     * when this method is called.</p>
     *
     * @param ctx   the current response context providing access to response details
     *              and mutable state
     * @param error the exception that caused the response processing failure
     * @since 1.0.0
     */
    default void onError(SynapseResponseContext ctx, SynapseException error) {}
}
