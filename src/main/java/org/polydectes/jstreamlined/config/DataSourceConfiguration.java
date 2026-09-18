package org.polydectes.jstreamlined.config;

import java.util.Locale;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfiguration {
    @Bean
    ConnectionDataSourceFactory connectionDataSourceFactory(ConnectionProperties properties) {
        return new ConnectionDataSourceFactory(properties);
    }

    public static final class ConnectionDataSourceFactory {
        private final ConnectionProperties properties;

        private ConnectionDataSourceFactory(ConnectionProperties properties) {
            this.properties = properties;
        }

        public DriverManagerDataSource create(String username, String password, String database) {
            String vendor = properties.vendor().toLowerCase(Locale.ROOT);
            String host = properties.host();
            int port = properties.port();
            String url = switch (vendor) {
                case "postgresql" -> "jdbc:postgresql://%s:%d/%s".formatted(host, port, database);
                case "mysql" -> "jdbc:mysql://%s:%d/%s".formatted(host, port, database);
                case "oracle" -> "jdbc:oracle:thin:@//%s:%d/%s".formatted(host, port, database);
                case "sqlserver" -> "jdbc:sqlserver://%s:%d;databaseName=%s".formatted(host, port, database);
                default -> throw new IllegalArgumentException("Unsupported database vendor: " + vendor);
            };
            DriverManagerDataSource dataSource = new DriverManagerDataSource(url, username, password);
            dataSource.setDriverClassName(switch (vendor) {
                case "postgresql" -> "org.postgresql.Driver";
                case "mysql" -> "com.mysql.cj.jdbc.Driver";
                case "oracle" -> "oracle.jdbc.OracleDriver";
                case "sqlserver" -> "com.microsoft.sqlserver.jdbc.SQLServerDriver";
                default -> throw new IllegalArgumentException("Unsupported database vendor: " + vendor);
            });
            return dataSource;
        }
    }
}
