package org.abhi.synapse.core.model;

public class ToolDefinition {
    private String type = "function";
    private FunctionDefinition function;
    public ToolDefinition() {}
    public ToolDefinition(FunctionDefinition function) { this.function = function; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public FunctionDefinition getFunction() { return function; }
    public void setFunction(FunctionDefinition function) { this.function = function; }
    public static ToolDefinition of(String name, String description, String parametersJson) {
        ToolDefinition td = new ToolDefinition();
        td.setFunction(new FunctionDefinition(name, description, parametersJson));
        return td;
    }
}
