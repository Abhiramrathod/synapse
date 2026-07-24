package org.abhi.synapse.interceptors;

import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.SynapseResponseContext;

public interface SynapseResponseInterceptor {
    default void beforeResponse(SynapseResponseContext ctx) {}
    default void afterResponse(SynapseResponseContext ctx) {}
    default void onError(SynapseResponseContext ctx, SynapseException error) {}
}
