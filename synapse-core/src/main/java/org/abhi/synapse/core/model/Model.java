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

    /**
     * No-argument constructor required for JSON deserialization.
     *
     * @since 1.0.0
     */
    public Model() {
    }

    /**
     * Constructs a new {@code Model} with the specified metadata.
     *
     * @param id       the unique identifier of the model (e.g. {@code "gpt-4"})
     * @param object   the object type, typically {@code "model"}
     * @param created  the Unix timestamp (in seconds) when the model was created
     * @param ownedBy  the organization that owns the model (e.g. {@code "openai"})
     * @since 1.0.0
     */
    public Model(String id, String object, long created, String ownedBy) {
        this.id = id;
        this.object = object;
        this.created = created;
        this.ownedBy = ownedBy;
    }

    /**
     * Returns the unique identifier of this model.
     *
     * @return the model ID, or {@code null} if not set
     * @since 1.0.0
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this model.
     *
     * @param id the model ID to set
     * @since 1.0.0
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the object type of this model.
     *
     * @return the object type (e.g. {@code "model"}), or {@code null} if not set
     * @since 1.0.0
     */
    public String getObject() {
        return object;
    }

    /**
     * Sets the object type of this model.
     *
     * @param object the object type to set
     * @since 1.0.0
     */
    public void setObject(String object) {
        this.object = object;
    }

    /**
     * Returns the Unix timestamp (in seconds) when this model was created.
     *
     * @return the creation timestamp, or {@code 0} if not set
     * @since 1.0.0
     */
    public long getCreated() {
        return created;
    }

    /**
     * Sets the Unix timestamp (in seconds) when this model was created.
     *
     * @param created the creation timestamp to set
     * @since 1.0.0
     */
    public void setCreated(long created) {
        this.created = created;
    }

    /**
     * Returns the organization that owns this model.
     *
     * @return the owner name, or {@code null} if not set
     * @since 1.0.0
     */
    public String getOwnedBy() {
        return ownedBy;
    }

    /**
     * Sets the organization that owns this model.
     *
     * @param ownedBy the owner name to set
     * @since 1.0.0
     */
    public void setOwnedBy(String ownedBy) {
        this.ownedBy = ownedBy;
    }
}
