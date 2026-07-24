package org.abhi.synapse.interceptors;

import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.SynapseRequestContext;

public interface SynapseRequestInterceptor {
    default void beforeRequest(SynapseRequestContext ctx) {}
    default void afterRequest(SynapseRequestContext ctx) {}
    default void onError(SynapseRequestContext ctx, SynapseException error) {}
}
