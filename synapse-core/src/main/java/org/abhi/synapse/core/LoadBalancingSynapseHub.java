package org.abhi.synapse.core;

import java.util.List;

/**
 * An {@link ISynapseHub} decorator that spreads load across multiple hubs using
 * thread-safe round-robin routing.
 *
 * <p>Each call is routed to the next hub in the list, so repeated prompts are
 * distributed evenly across the configured hubs. This is useful for scaling out
 * across multiple provider accounts, regions, or API keys:</p>
 *
 * <pre>{@code
 * ISynapseHub hubA = new SynapseHub(configA);
 * ISynapseHub hubB = new SynapseHub(configB);
 * ISynapseHub hub = new LoadBalancingSynapseHub(hubA, hubB);
 * }</pre>
 *
 * <p>Unlike {@link FallbackSynapseHub}, calls are never retried on another hub;
 * a failing hub propagates its error to the caller. Combine the two if you want
 * both distribution and resilience.</p>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 * @see FallbackSynapseHub
 */
public class LoadBalancingSynapseHub extends AbstractDelegatingHub {

    public LoadBalancingSynapseHub(ISynapseHub... hubs) {
        super(List.of(hubs));
    }

    public LoadBalancingSynapseHub(List<ISynapseHub> hubs) {
        super(hubs);
    }

    @Override
    protected int nextHubIndex() {
        return roundRobin();
    }
}
