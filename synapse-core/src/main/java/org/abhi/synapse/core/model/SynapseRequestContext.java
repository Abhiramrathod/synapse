package org.abhi.synapse.core.model;

public class SynapseRequestContext {

    private String url;
    private String body;
    private final java.util.Map<String, String> headers;
    private final boolean streaming;
    private final String model;

    public SynapseRequestContext(String url, String body, java.util.Map<String, String> headers,
                                 boolean streaming, String model) {
        this.url = url;
        this.body = body;
        this.headers = new java.util.HashMap<>(headers);
        this.streaming = streaming;
        this.model = model;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public java.util.Map<String, String> getHeaders() {
        return java.util.Map.copyOf(headers);
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public void removeHeader(String name) {
        headers.remove(name);
    }

    public boolean isStreaming() {
        return streaming;
    }

    public String getModel() {
        return model;
    }
}
