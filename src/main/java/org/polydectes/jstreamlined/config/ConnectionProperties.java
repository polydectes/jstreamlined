package org.polydectes.jstreamlined.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("jstreamlined.datasource")
public record ConnectionProperties(String vendor, String host, Integer port, String database,
                                   String username, String password, String schema) {
}
