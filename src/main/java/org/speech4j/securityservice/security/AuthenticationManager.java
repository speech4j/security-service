package org.speech4j.securityservice.security;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.speech4j.securityservice.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class AuthenticationManager implements ReactiveAuthenticationManager {

    @Autowired
    private JWTUtil jwtUtil;

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();
        LOGGER.debug("RAZDVATRY {}", authToken);
        try {
            if (!jwtUtil.validateToken(authToken)) {
                LOGGER.debug("TRYDVARAZ");
                return Mono.empty();
            }
            Claims claims = jwtUtil.getAllClaimsFromToken(authToken);
            return Mono.just(new UsernamePasswordAuthenticationToken(claims.getSubject(), null, null));
        } catch (Exception e) {
            return Mono.empty();
        }
    }
}
