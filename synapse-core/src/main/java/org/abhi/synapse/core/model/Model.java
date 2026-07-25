package org.abhi.synapse.core.model;

/**
 * Represents a model available from an LLM API endpoint.
 *
 * <p>{@code Model} encapsulates metadata about a model returned by the
 * {@code /v1/models} endpoint, following the OpenAI-compatible model list format.</p>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 * @see org.abhi.synapse.core.ISynapseHub#getModelsList()
 */
public class Model {

    private String id;
    private String object;
    private long created;
    private String ownedBy;

    public Model() {
    }

    public Model(String id, String object, long created, String ownedBy) {
        this.id = id;
        this.object = object;
        this.created = created;
        this.ownedBy = ownedBy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getOwnedBy() {
        return ownedBy;
    }

    public void setOwnedBy(String ownedBy) {
        this.ownedBy = ownedBy;
    }
}
