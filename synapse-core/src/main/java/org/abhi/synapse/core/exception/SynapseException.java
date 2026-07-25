package org.abhi.synapse.core.exception;

/**
 * Custom runtime exception thrown when Synapse LLM operations fail.
 *
 * <p>{@code SynapseException} encapsulates all error conditions that may arise during
 * interactions with LLM services, including configuration errors, network failures,
 * rate limiting, server errors, and parsing issues. Each exception carries an
 * {@link ExceptionType} that categorizes the error, enabling callers to implement
 * appropriate error handling and retry strategies.</p>
 *
 * <p>When the exception is caused by an HTTP response, it includes the status code
 * and response body from the LLM provider for diagnostic purposes.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * try {
 *     SynapseResponse response = hub.sendPrompt("Hello");
 * } catch (SynapseException e) {
 *     if (e.isRetryable()) {
 *         // Retry the request
 *     } else if (e.getType() == ExceptionType.RATE_LIMIT_ERROR) {
 *         // Back off and retry later
 *     } else {
 *         // Log and handle the error
 *         log.error("LLM call failed: {}", e.getMessage());
 *     }
 * }
 * }</pre>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 * @see ExceptionType
 * @see #isRetryable()
 */
public class SynapseException extends RuntimeException {

    /** HTTP status code returned by the server, or {@code -1} if no HTTP response was received. */
    private int statusCode;
    /** Raw response body from the server, if available. */
    private String responseBody;
    /** Categorized exception type indicating the nature of the failure. */
    private ExceptionType type;

    /**
     * Enumeration of exception types categorizing the cause of a Synapse failure.
     *
     * <p>Each type represents a distinct failure category that callers can use to
     * implement targeted error handling and retry strategies.</p>
     *
     * @author Abhiram Rathod
     * @since 1.0.0
     */
    public enum ExceptionType {
        /** The exception was caused by an invalid or missing configuration setting. */
        CONFIG_ERROR,

        /** The exception was caused by a network connectivity issue. */
        NETWORK_ERROR,

        /** The exception was caused by an operation exceeding its timeout limit. */
        TIMEOUT_ERROR,

        /** The exception was caused by the LLM provider rate-limiting the request (HTTP 429). */
        RATE_LIMIT_ERROR,

        /** The exception was caused by an LLM provider server error (HTTP 5xx). */
        SERVER_ERROR,

        /** The exception was caused by a failure to parse the LLM response. */
        PARSE_ERROR,

        /** The exception was caused by a failure during response streaming. */
        STREAMING_ERROR,

        /** The exception was caused by exhausting all retry attempts. */
        RETRY_EXHAUSTED
    }

    /**
     * Constructs a new {@code SynapseException} with the specified message and
     * a default type of {@link ExceptionType#CONFIG_ERROR}.
     *
     * @param message the detail message describing the exception
     * @since 1.0.0
     */
    public SynapseException(String message) {
        super(message);
        this.type = ExceptionType.CONFIG_ERROR;
    }

    /**
     * Constructs a new {@code SynapseException} with the specified message, cause,
     * and a default type of {@link ExceptionType#NETWORK_ERROR}.
     *
     * @param message the detail message describing the exception
     * @param cause   the underlying cause of this exception
     * @since 1.0.0
     */
    public SynapseException(String message, Throwable cause) {
        super(message, cause);
        this.type = ExceptionType.NETWORK_ERROR;
    }

    /**
     * Constructs a new {@code SynapseException} from an HTTP status code and response body.
     *
     * <p>The {@link ExceptionType} is automatically determined by mapping the status code:
     * <ul>
     *   <li>429 &rarr; {@link ExceptionType#RATE_LIMIT_ERROR}</li>
     *   <li>500+ &rarr; {@link ExceptionType#SERVER_ERROR}</li>
     *   <li>Other &rarr; {@link ExceptionType#NETWORK_ERROR}</li>
     * </ul>
     *
     * @param statusCode   the HTTP status code from the LLM provider response
     * @param responseBody the raw response body from the LLM provider
     * @since 1.0.0
     */
    public SynapseException(int statusCode, String responseBody) {
        super("LLM call failed with status " + statusCode);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.type = mapStatusCode(statusCode);
    }

    /**
     * Constructs a new {@code SynapseException} with the specified message and exception type.
     *
     * @param message the detail message describing the exception
     * @param type    the {@link ExceptionType} categorizing this exception
     * @since 1.0.0
     */
    public SynapseException(String message, ExceptionType type) {
        super(message);
        this.type = type;
    }

    /**
     * Constructs a new {@code SynapseException} with the specified message, cause,
     * and exception type.
     *
     * @param message the detail message describing the exception
     * @param cause   the underlying cause of this exception
     * @param type    the {@link ExceptionType} categorizing this exception
     * @since 1.0.0
     */
    public SynapseException(String message, Throwable cause, ExceptionType type) {
        super(message, cause);
        this.type = type;
    }

    /**
     * Maps an HTTP status code to the corresponding {@link ExceptionType}.
     *
     * @param statusCode the HTTP status code to map
     * @return the mapped {@link ExceptionType}
     * @since 1.0.0
     */
    private ExceptionType mapStatusCode(int statusCode) {
        if (statusCode == 429) {
            return ExceptionType.RATE_LIMIT_ERROR;
        } else if (statusCode >= 500) {
            return ExceptionType.SERVER_ERROR;
        }
        return ExceptionType.NETWORK_ERROR;
    }

    /**
     * Returns the HTTP status code from the LLM provider response, if available.
     *
     * @return the HTTP status code, or {@code 0} if the exception was not caused by an HTTP response
     * @since 1.0.0
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the raw response body from the LLM provider, if available.
     *
     * @return the response body string, or {@code null} if the exception was not caused by an HTTP response
     * @since 1.0.0
     */
    public String getResponseBody() {
        return responseBody;
    }

    /**
     * Returns the {@link ExceptionType} categorizing this exception.
     *
     * @return the exception type
     * @since 1.0.0
     */
    public ExceptionType getType() {
        return type;
    }

    /**
     * Determines whether this exception represents a transient failure that can be retried.
     *
     * <p>Returns {@code true} for {@link ExceptionType#RATE_LIMIT_ERROR},
     * {@link ExceptionType#SERVER_ERROR}, {@link ExceptionType#NETWORK_ERROR},
     * and {@link ExceptionType#TIMEOUT_ERROR}. Returns {@code false} for
     * configuration errors, parsing errors, and exhausted retries.</p>
     *
     * @return {@code true} if the request can be retried, {@code false} otherwise
     * @since 1.0.0
     */
    public boolean isRetryable() {
        return type == ExceptionType.RATE_LIMIT_ERROR
                || type == ExceptionType.SERVER_ERROR
                || type == ExceptionType.NETWORK_ERROR
                || type == ExceptionType.TIMEOUT_ERROR;
    }
}
