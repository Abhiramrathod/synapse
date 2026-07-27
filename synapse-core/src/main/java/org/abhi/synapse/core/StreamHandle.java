package org.abhi.synapse.core;

import org.abhi.synapse.core.model.SynapseResponse;
import java.util.concurrent.CompletableFuture;

public class StreamHandle {
    private final CancellationToken cancellationToken;
    private final CompletableFuture<SynapseResponse> future;
    public StreamHandle(CancellationToken cancellationToken, CompletableFuture<SynapseResponse> future) {
        this.cancellationToken = cancellationToken;
        this.future = future;
    }
    public void cancel() { cancellationToken.cancel(); }
    public boolean isCancelled() { return cancellationToken.isCancelled(); }
    public CompletableFuture<SynapseResponse> getFuture() { return future; }
}
