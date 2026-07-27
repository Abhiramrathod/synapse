package org.abhi.synapse.core;

import org.abhi.synapse.core.exception.SynapseException;
import java.util.concurrent.atomic.AtomicBoolean;

public class CancellationToken {
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    public boolean isCancelled() { return cancelled.get(); }
    public void cancel() { cancelled.set(true); }
    public void throwIfCancelled() throws SynapseException {
        if (cancelled.get()) throw new SynapseException("Stream cancelled by caller",
                SynapseException.ExceptionType.STREAMING_ERROR);
    }
}
