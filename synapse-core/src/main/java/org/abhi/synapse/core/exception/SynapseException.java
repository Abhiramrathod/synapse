package org.abhi.synapse.core.exception;

public class SynapseException extends RuntimeException {

    private int statusCode;
    private String responseBody;
    private ExceptionType type;

    public enum ExceptionType {
        CONFIG_ERROR,
        NETWORK_ERROR,
        TIMEOUT_ERROR,
        RATE_LIMIT_ERROR,
        SERVER_ERROR,
        PARSE_ERROR,
        STREAMING_ERROR,
        RETRY_EXHAUSTED
    }

    public SynapseException(String message) {
        super(message);
        this.type = ExceptionType.CONFIG_ERROR;
    }

    public SynapseException(String message, Throwable cause) {
        super(message, cause);
        this.type = ExceptionType.NETWORK_ERROR;
    }

    public SynapseException(int statusCode, String responseBody) {
        super("LLM call failed with status " + statusCode);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.type = mapStatusCode(statusCode);
    }

    public SynapseException(String message, ExceptionType type) {
        super(message);
        this.type = type;
    }

    public SynapseException(String message, Throwable cause, ExceptionType type) {
        super(message, cause);
        this.type = type;
    }

    private ExceptionType mapStatusCode(int statusCode) {
        if (statusCode == 429) {
            return ExceptionType.RATE_LIMIT_ERROR;
        } else if (statusCode >= 500) {
            return ExceptionType.SERVER_ERROR;
        }
        return ExceptionType.NETWORK_ERROR;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public ExceptionType getType() {
        return type;
    }

    public boolean isRetryable() {
        return type == ExceptionType.RATE_LIMIT_ERROR
                || type == ExceptionType.SERVER_ERROR
                || type == ExceptionType.NETWORK_ERROR
                || type == ExceptionType.TIMEOUT_ERROR;
    }
}
