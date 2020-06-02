package org.speech4j.securityservice.config;

import org.speech4j.securityservice.security.AuthenticationManager;
import org.speech4j.securityservice.security.SecurityContextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class WebSecurityConfig {

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private SecurityContextRepository repository;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .exceptionHandling()
                .authenticationEntryPoint((swe, e) ->
                    Mono.fromRunnable(() ->
                        swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED)
                    )
                ).accessDeniedHandler((swe, e) ->
                    Mono.fromRunnable(() ->
                        swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN)
                    )
                ).and()
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .authenticationManager(manager)
                .securityContextRepository(repository)
                .authorizeExchange()
                .pathMatchers("/register").permitAll()
                .pathMatchers("/login").permitAll()
                .pathMatchers(HttpMethod.DELETE, "/users").hasAuthority("admin")
                .anyExchange().authenticated()
                .and().build();
    }
}
