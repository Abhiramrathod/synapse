package org.abhi.synapse.core.model;

public class ToolCall {
    private String id;
    private String type;
    private String function;
    private String arguments;
    public ToolCall() {}
    public ToolCall(String id, String type, String function, String arguments) {
        this.id = id; this.type = type; this.function = function; this.arguments = arguments;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getFunction() { return function; }
    public void setFunction(String function) { this.function = function; }
    public String getArguments() { return arguments; }
    public void setArguments(String arguments) { this.arguments = arguments; }
}
