package org.abhi.synapse.spring;

import org.abhi.synapse.config.SynapseConfig;
import org.abhi.synapse.http.SynapseHub;
import org.abhi.synapse.metrics.SynapseMetrics;
import org.abhi.synapse.core.ISynapseHub;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Spring Boot auto-configuration for Synapse.
 *
 * <p>Automatically configures {@link org.abhi.synapse.core.ISynapseHub} beans
 * when Synapse properties are present in the application configuration. Registers
 * default beans for {@link SynapseConfig}, {@link ISynapseHub}, and
 * {@link SynapseMetrics} only when no user-defined bean of the same type exists.</p>
 *
 * <p>Enable by adding {@code @EnableAutoConfiguration} or including the
 * {@code synapse-spring-boot-starter} dependency on the classpath. Configuration
 * properties are bound from the {@code synapse.*} prefix via {@link SynapseProperties}.</p>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 * @see SynapseProperties
 * @see SynapseConfig
 * @see ISynapseHub
 */
@Configuration
@EnableConfigurationProperties(SynapseProperties.class)
public class SynapseAutoConfiguration {

    /**
     * Creates a {@link SynapseConfig} bean from the bound {@link SynapseProperties}.
     *
     * <p>This bean is only registered if no other {@link SynapseConfig} bean is
     * already present in the application context, allowing users to override
     * configuration by declaring their own bean.</p>
     *
     * @param properties the {@link SynapseProperties} bound from {@code synapse.*} configuration
     * @return a new {@link SynapseConfig} instance built from the properties
     * @since 1.0.0
     */
    @Bean
    @ConditionalOnMissingBean
    public SynapseConfig synapseConfig(SynapseProperties properties) {
        return properties.toSynapseConfig();
    }

    /**
     * Creates the default {@link ISynapseHub} implementation backed by {@link SynapseHub}.
     *
     * <p>This bean is only registered if no other {@link ISynapseHub} bean is already
     * present, enabling users to substitute their own hub implementation if needed.</p>
     *
     * @param config the {@link SynapseConfig} providing connection and model settings
     * @return a new {@link SynapseHub} instance configured with the provided settings
     * @since 1.0.0
     */
    @Bean
    @ConditionalOnMissingBean
    public ISynapseHub synapseHub(SynapseConfig config) {
        return new SynapseHub(config);
    }

    /**
     * Creates a default {@link SynapseMetrics} bean for recording request-level metrics.
     *
     * <p>This bean is only registered if no other {@link SynapseMetrics} bean is
     * already present, allowing users to supply a custom metrics implementation.</p>
     *
     * @return a new {@link SynapseMetrics} instance backed by in-memory storage
     * @since 1.0.0
     */
    @Bean
    @ConditionalOnMissingBean
    public SynapseMetrics synapseMetrics() {
        return new SynapseMetrics();
    }
}
