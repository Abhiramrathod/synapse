package org.abhi.synapse.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CancellationTokenTest {

    @Test
    void initiallyNotCancelled() {
        CancellationToken token = new CancellationToken();
        assertThat(token.isCancelled()).isFalse();
    }

    @Test
    void cancelMarksAsCancelled() {
        CancellationToken token = new CancellationToken();
        token.cancel();
        assertThat(token.isCancelled()).isTrue();
    }

    @Test
    void cancelIsIdempotent() {
        CancellationToken token = new CancellationToken();
        token.cancel();
        token.cancel();
        assertThat(token.isCancelled()).isTrue();
    }
}
