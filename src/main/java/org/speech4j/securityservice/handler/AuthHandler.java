package org.speech4j.securityservice.handler;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.speech4j.securityservice.domain.User;
import org.speech4j.securityservice.dto.AuthRequest;
import org.speech4j.securityservice.dto.AuthResponse;
import org.speech4j.securityservice.dto.UserDto;
import org.speech4j.securityservice.dto.validation.New;
import org.speech4j.securityservice.service.UserService;
import org.speech4j.securityservice.service.UserServiceImpl;
import org.speech4j.securityservice.util.JWTUtil;
import org.speech4j.securityservice.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
@Slf4j
public class AuthHandler {

    private UserService service;
    private Validator validator;
    private PasswordEncoder encoder;
    private JWTUtil jwtUtil;
    private ValidationUtil validationUtil;
    private ModelMapper mapper = new ModelMapper();

    @Autowired
    public AuthHandler(UserService service, Validator validator,
                       PasswordEncoder encoder, JWTUtil jwtUtil,
                       ValidationUtil validationUtil) {
        this.service = service;
        this.validator = validator;
        this.validationUtil = validationUtil;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(AuthRequest.class).flatMap(body -> {
            Set<ConstraintViolation<AuthRequest>> errors = validator.validate(body, New.class);
            if (!errors.isEmpty()) {
                return validationUtil.validateMono(errors);
            } else {
                return service.findByUsername(body.getUsername()).flatMap(user -> {
                    if (encoder.matches(body.getPassword(), user.getPassword())) {
                        String token = jwtUtil.generateToken(user);
                        AuthResponse response = new AuthResponse(token);
                        return ServerResponse.ok()
                                .contentType(APPLICATION_JSON)
                                .body(fromValue(response));
                    } else {
                        LOGGER.debug("Password doesn't match");
                        return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
                    }
                }).doOnError(err -> LOGGER.error("Error {}", err));
            }
        });
    }

    public Mono<ServerResponse> register(ServerRequest request) {
        return request.bodyToMono(UserDto.class).flatMap(body -> {
            Set<ConstraintViolation<UserDto>> errors = validator.validate(body, New.class);
            if (!errors.isEmpty()) {
                return validationUtil.validateMono(errors);
            } else {
                return ServerResponse.status(HttpStatus.CREATED)
                        .contentType(APPLICATION_JSON)
                        .body(service.create(body), UserDto.class);
            }
        });
    }

}
