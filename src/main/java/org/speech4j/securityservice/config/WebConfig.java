package org.speech4j.securityservice.config;

import com.nimbusds.jose.util.IntegerUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.speech4j.securityservice.handler.AuthHandler;
import org.speech4j.securityservice.handler.RoleHandler;
import org.speech4j.securityservice.handler.UserHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer {

    @Bean
    RouterFunction<ServerResponse> userRoutes(UserHandler handler) {
        return route(GET("/users").and(accept(APPLICATION_JSON)), handler::getUsers)
                .andRoute(GET("/users").and(hasQueryParam("email")), handler::getUserByEmail)
                .andRoute(GET("/users").and(hasQueryParam("username")), handler::getUserByUsername)
                .andRoute(GET("/users/{id}").and(accept(APPLICATION_JSON)), handler::getUserById)
                .andRoute(PUT("/users/{id}").and(accept(APPLICATION_JSON)), handler::updateUser)
                .andRoute(DELETE("/users/{id}").and(accept(APPLICATION_JSON)), handler::deleteUser)
                .andRoute(GET("/users/{id}/roles").and(accept(APPLICATION_JSON)), handler::getRolesByUserId)
                .andRoute(POST("/users/{id}/roles").and(accept(APPLICATION_JSON)), handler::addRoleToUser)
                .andRoute(DELETE("/users/{userId}/roles/{roleId}").and(accept(APPLICATION_JSON)), handler::removeRoleFromUser);
    }

    @Bean
    RouterFunction<ServerResponse> authRoutes(AuthHandler handler) {
        return route(POST("/login").and(accept(APPLICATION_JSON)), handler::login)
               .andRoute(POST("/register").and(accept(APPLICATION_JSON)), handler::register);
    }

    @Bean
    RouterFunction<ServerResponse> roleRoutes(RoleHandler handler) {
        return route(POST("/roles").and(accept(APPLICATION_JSON)), handler::createRole)
                .andRoute(GET("/roles"), handler::getRoles)
                .andRoute(GET("/roles/{id}"), handler::getRoleById)
                .andRoute(PUT("/roles/{id}").and(accept(APPLICATION_JSON)), handler::updateRole)
                .andRoute(DELETE("/roles/{id}"), handler::deleteRole);
    }

    public static RequestPredicate hasQueryParam(String name) {
        return RequestPredicates.queryParam(name, StringUtils::hasText);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
