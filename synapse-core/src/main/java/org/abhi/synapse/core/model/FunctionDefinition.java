package org.abhi.synapse.core.model;

public class FunctionDefinition {
    private String name;
    private String description;
    private String parameters;
    public FunctionDefinition() {}
    public FunctionDefinition(String name, String description, String parameters) {
        this.name = name; this.description = description; this.parameters = parameters;
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }
}
