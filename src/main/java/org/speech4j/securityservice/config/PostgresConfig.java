package org.speech4j.securityservice.config;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@EnableR2dbcRepositories
public class PostgresConfig extends AbstractR2dbcConfiguration {

    @Value("${spring.data.postgres.host}") private String host;
    @Value("${spring.data.postgres.port}") private int port;
    @Value("${spring.data.postgres.database}") private String database;
    @Value("${spring.data.postgres.username}") private String username;
    @Value("${spring.data.postgres.password}") private String password;

    @Bean
    public PostgresqlConnectionFactory connectionFactory() {
        return new PostgresqlConnectionFactory(PostgresqlConnectionConfiguration.builder()
                .host(host)
                .port(port)
                .database(database)
                .username(username)
                .password(password)
                .build());
    }
}
