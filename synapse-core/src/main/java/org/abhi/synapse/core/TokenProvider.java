package org.abhi.synapse.core;

import java.util.function.Supplier;

/**
 * Supplies the authentication credential for outbound requests at call time.
 *
 * <p>Unlike a static {@code apiKey}, a {@code TokenProvider} is invoked on
 * every request, so credentials can rotate without restarting the application.
 * This is the extension point for enterprise identity flows:</p>
 *
 * <ul>
 *   <li>AWS Bedrock SigV4 signers that produce per-request signatures</li>
 *   <li>Azure OpenAI Entra ID / Managed Identity token acquisition</li>
 *   <li>Short-lived access tokens refreshed by a background process</li>
 * </ul>
 *
 * <pre>{@code
 * TokenProvider provider = TokenProvider.fromSupplier(MyTokenService::currentToken);
 * SynapseConfig config = SynapseConfig.builder()
 *         .tokenProvider(provider)   // replaces .apiKey(...)
 *         ...
 *         .build();
 * }</pre>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 */
@FunctionalInterface
public interface TokenProvider {

    /**
     * Returns the current credential token.
     *
     * <p>The value is whatever the provider needs in the Authorization header,
     * without the scheme prefix (e.g. the raw JWT or access token).</p>
     *
     * @return the current token; never {@code null}
     */
    String getToken();

    /**
     * Builds the value of the {@code Authorization} header for the current token.
     *
     * <p>The default prefixes the token with {@code "Bearer "}. Providers such
     * as AWS SigV4 that need a different scheme override this method.</p>
     *
     * @return the full Authorization header value
     */
    default String buildAuthorizationHeader() {
        return "Bearer " + getToken();
    }

    /**
     * Wraps a plain bearer token.
     *
     * @param token the bearer token
     * @return a provider that always returns the given token
     */
    static TokenProvider bearer(String token) {
        return () -> token;
    }

    /**
     * Adapts a {@link Supplier} into a {@link TokenProvider}.
     *
     * @param supplier the token source, called on every request
     * @return a provider backed by the supplier
     */
    static TokenProvider fromSupplier(Supplier<String> supplier) {
        return supplier::get;
    }
}
