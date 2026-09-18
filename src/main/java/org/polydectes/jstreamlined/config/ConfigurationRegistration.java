package org.polydectes.jstreamlined.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ConnectionProperties.class)
public class ConfigurationRegistration {
}
