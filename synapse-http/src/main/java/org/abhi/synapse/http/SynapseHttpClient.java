package org.abhi.synapse.http;

import org.abhi.synapse.core.exception.SynapseException;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Stream;

class SynapseHttpClient {

    private final HttpClient httpClient;

    SynapseHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    HttpResponse<String> send(HttpRequest request) throws SynapseException {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (java.net.http.HttpTimeoutException e) {
            throw new SynapseException("Request timed out", e,
                    SynapseException.ExceptionType.TIMEOUT_ERROR);
        } catch (Exception e) {
            throw new SynapseException("Request failed", e,
                    SynapseException.ExceptionType.NETWORK_ERROR);
        }
    }

    HttpResponse<Stream<String>> sendStreaming(HttpRequest request) throws SynapseException {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofLines());
        } catch (java.net.http.HttpTimeoutException e) {
            throw new SynapseException("Streaming request timed out", e,
                    SynapseException.ExceptionType.TIMEOUT_ERROR);
        } catch (Exception e) {
            throw new SynapseException("Streaming request failed", e,
                    SynapseException.ExceptionType.NETWORK_ERROR);
        }
    }
}
