package org.abhi.synapse.http;

import org.abhi.synapse.core.ProviderAdapter;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.Model;
import org.abhi.synapse.core.model.SynapseResponse;

import java.util.List;

/**
 * Parses JSON response bodies from LLM API endpoints into {@link SynapseResponse} objects.
 *
 * <p>This class delegates parsing to the configured {@link ProviderAdapter} so that
 * each provider controls how its responses are interpreted.</p>
 *
 * <p>This is an internal class within the {@code synapse-http} module and is not
 * intended for direct use by library consumers.</p>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 * @see SynapseResponse
 * @see ProviderAdapter
 */
class SynapseResponseParser {

    private final ProviderAdapter adapter;

    /**
     * Constructs a new {@code SynapseResponseParser} with the specified provider adapter.
     *
     * @param adapter the {@link ProviderAdapter} used to parse responses; must not be {@code null}
     * @since 1.0.0
     */
    SynapseResponseParser(ProviderAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * Parses a JSON response body string into a {@link SynapseResponse} object
     * using the configured provider adapter.
     *
     * @param responseBody the JSON response body string; must not be {@code null}
     * @return the parsed {@link SynapseResponse}; never {@code null}
     * @throws SynapseException if the response body cannot be parsed as valid JSON
     *                          for the configured provider
     * @since 1.0.0
     */
    SynapseResponse parse(String responseBody) throws SynapseException {
        return adapter.parseResponse(responseBody);
    }

    List<Model> parseModels(String responseBody) throws SynapseException {
        return adapter.parseModels(responseBody);
    }
}
