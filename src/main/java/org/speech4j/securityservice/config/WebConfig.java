package org.speech4j.securityservice.config;

import org.speech4j.securityservice.handler.UserHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer {

    @Bean
    RouterFunction<ServerResponse> routes(UserHandler handler) {
        return route(GET("/users").and(accept(APPLICATION_JSON)), handler::getUsers)
                .andRoute(POST("/users").and(accept(APPLICATION_JSON)), handler::createUser)
                .andRoute(GET("/users/{id}").and(accept(APPLICATION_JSON)), handler::getUserById)
                .andRoute(PUT("/users/{id}").and(accept(APPLICATION_JSON)), handler::updateUser)
                .andRoute(DELETE("/users/{id}").and(accept(APPLICATION_JSON)), handler::deleteUser);
    }

}
