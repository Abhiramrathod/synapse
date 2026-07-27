package org.abhi.synapse.core;

import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.SynapseResponse;
import java.util.function.Consumer;

public interface StreamListener {
    void onChunk(String text);
    void onComplete(SynapseResponse fullResponse);
    void onError(SynapseException error);
    static StreamListener of(Consumer<String> onChunk) {
        return new StreamListener() {
            @Override public void onChunk(String text) { onChunk.accept(text); }
            @Override public void onComplete(SynapseResponse fullResponse) {}
            @Override public void onError(SynapseException error) {}
        };
    }
}
