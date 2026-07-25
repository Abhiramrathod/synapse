package org.abhi.synapse.interceptors;

import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.SynapseRequestContext;

/**
 * Interceptor for the request phase of the Synapse HTTP communication lifecycle.
 *
 * <p>Implementations of this interface can hook into the request pipeline to perform actions
 * before a request is sent, after a request has been dispatched, or when a request error occurs.
 * This is useful for cross-cutting concerns such as logging, metrics collection, request
 * enrichment, authentication token injection, and header manipulation.</p>
 *
 * <p>All methods have default (no-op) implementations so that implementors may override only
 * the callbacks they need.</p>
 *
 * <p>Implementation example:</p>
 * <pre>{@code
 * public class LoggingRequestInterceptor implements SynapseRequestInterceptor {
 *
 *     private static final Logger log = LoggerFactory.getLogger(LoggingRequestInterceptor.class);
 *
 *     @Override
 *     public void beforeRequest(SynapseRequestContext ctx) {
 *         log.info("Sending request to {} with correlationId={}",
 *                 ctx.getTargetUrl(), ctx.getCorrelationId());
 *     }
 *
 *     @Override
 *     public void afterRequest(SynapseRequestContext ctx) {
 *         log.info("Request dispatched successfully, correlationId={}",
 *                 ctx.getCorrelationId());
 *     }
 *
 *     @Override
 *     public void onError(SynapseRequestContext ctx, SynapseException error) {
 *         log.error("Request failed for correlationId={}: {}",
 *                 ctx.getCorrelationId(), error.getMessage());
 *     }
 * }
 * }</pre>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 * @see SynapseResponseInterceptor
 * @see SynapseRequestContext
 */
public interface SynapseRequestInterceptor {

    /**
     * Called immediately before a request is dispatched over the network.
     *
     * <p>Use this callback to inspect or modify the {@link SynapseRequestContext},
     * such as adding custom headers, enriching the payload, or recording the
     * start time for latency measurements.</p>
     *
     * @param ctx the current request context providing access to request details
     *            and mutable state
     * @since 1.0.0
     */
    default void beforeRequest(SynapseRequestContext ctx) {}

    /**
     * Called after a request has been successfully dispatched over the network.
     *
     * <p>Use this callback for post-send tasks such as logging a successful
     * dispatch, recording metrics, or updating a circuit-breaker state.</p>
     *
     * @param ctx the current request context providing access to request details
     *            and mutable state
     * @since 1.0.0
     */
    default void afterRequest(SynapseRequestContext ctx) {}

    /**
     * Called when a request fails with an error before or during dispatch.
     *
     * <p>Use this callback to perform error-specific handling such as logging
     * the failure, incrementing error counters, or triggering alerting. The
     * request pipeline will not invoke {@link #afterRequest(SynapseRequestContext)}
     * when this method is called.</p>
     *
     * @param ctx   the current request context providing access to request details
     *              and mutable state
     * @param error the exception that caused the request failure
     * @since 1.0.0
     */
    default void onError(SynapseRequestContext ctx, SynapseException error) {}
}
