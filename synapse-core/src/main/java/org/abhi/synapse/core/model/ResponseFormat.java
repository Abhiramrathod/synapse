package org.abhi.synapse.core.model;

public class ResponseFormat {
    private String type;
    public ResponseFormat() {}
    public ResponseFormat(String type) { this.type = type; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public static ResponseFormat json() { return new ResponseFormat("json_object"); }
    public static ResponseFormat text() { return new ResponseFormat("text"); }
}
