package org.abhi.synapse.spring;

import org.abhi.synapse.config.SynapseConfig;
import org.abhi.synapse.http.SynapseHub;
import org.abhi.synapse.metrics.SynapseMetrics;
import org.abhi.synapse.core.ISynapseHub;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(SynapseProperties.class)
public class SynapseAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SynapseConfig synapseConfig(SynapseProperties properties) {
        return properties.toSynapseConfig();
    }

    @Bean
    @ConditionalOnMissingBean
    public ISynapseHub synapseHub(SynapseConfig config) {
        return new SynapseHub(config);
    }

    @Bean
    @ConditionalOnMissingBean
    public SynapseMetrics synapseMetrics() {
        return new SynapseMetrics();
    }
}
