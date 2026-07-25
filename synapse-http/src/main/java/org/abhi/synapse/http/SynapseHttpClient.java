package org.abhi.synapse.http;

import org.abhi.synapse.core.exception.SynapseException;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Stream;

/**
 * Low-level HTTP client wrapper that delegates to {@link HttpClient} for sending
 * synchronous and streaming HTTP requests. This class translates low-level
 * {@link java.net.http} exceptions into {@link SynapseException} with appropriate
 * exception types for standardized error handling throughout the framework.
 *
 * <p>This is an internal class within the {@code synapse-http} module and is not
 * intended for direct use by library consumers.</p>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 * @see SynapseHub
 * @see SynapseStreamHandler
 */
class SynapseHttpClient {

    private final HttpClient httpClient;

    /**
     * Constructs a new {@code SynapseHttpClient} wrapping the specified
     * {@link HttpClient}.
     *
     * @param httpClient the {@link HttpClient} to delegate requests to;
     *                   must not be {@code null}
     * @since 1.0.0
     */
    SynapseHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Sends an HTTP request synchronously and returns the response as a
     * {@link String}.
     *
     * <p>If the request times out, a {@link SynapseException} with type
     * {@link SynapseException.ExceptionType#TIMEOUT_ERROR} is thrown. For all
     * other failures, a {@link SynapseException} with type
     * {@link SynapseException.ExceptionType#NETWORK_ERROR} is thrown.</p>
     *
     * @param request the {@link HttpRequest} to send; must not be {@code null}
     * @return the {@link HttpResponse} containing the response body as a {@link String};
     *         never {@code null}
     * @throws SynapseException if the request times out or fails due to a network error
     * @since 1.0.0
     */
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

    /**
     * Sends an HTTP request for streaming and returns the response body as a
     * {@link Stream} of lines.
     *
     * <p>This method is used for Server-Sent Events (SSE) streaming, where the
     * response body is delivered line-by-line. If the request times out, a
     * {@link SynapseException} with type
     * {@link SynapseException.ExceptionType#TIMEOUT_ERROR} is thrown. For all
     * other failures, a {@link SynapseException} with type
     * {@link SynapseException.ExceptionType#NETWORK_ERROR} is thrown.</p>
     *
     * @param request the {@link HttpRequest} to send; must not be {@code null}
     * @return the {@link HttpResponse} containing the response body as a {@link Stream}
     *         of {@link String} lines; never {@code null}
     * @throws SynapseException if the streaming request times out or fails due to a network error
     * @since 1.0.0
     */
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
