package org.speech4j.securityservice.handler;

import lombok.extern.slf4j.Slf4j;
import org.speech4j.securityservice.dto.AuthRequest;
import org.speech4j.securityservice.dto.AuthResponse;
import org.speech4j.securityservice.dto.UserDto;
import org.speech4j.securityservice.dto.validation.Existing;
import org.speech4j.securityservice.dto.validation.New;
import org.speech4j.securityservice.service.UserService;
import org.speech4j.securityservice.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Slf4j
@Component
public class UserHandler {

    private UserService service;
    private Validator validator;
    private PasswordEncoder encoder;
    private JWTUtil jwtUtil;
    private Map<String, String> responseBody;
    private static final Integer MAX = 10;
    private static final Integer OFFSET = 0;

    @Autowired
    public UserHandler(UserService service, Validator validator, PasswordEncoder encoder, JWTUtil jwtUtil) {
        this.service = service;
        this.validator = validator;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
        this.responseBody = new HashMap<>();
    }

    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(AuthRequest.class).flatMap(body -> {
            Set<ConstraintViolation<AuthRequest>> errors = validator.validate(body, New.class);
            if (!errors.isEmpty()) {
                return validateMono(errors);
            } else {
            return service.getByEmail(body.getEmail()).flatMap(user -> {
                if (encoder.matches(body.getPassword(), user.getPassword())) {
                        AuthResponse response = new AuthResponse(jwtUtil.generateToken(user));
                        return ServerResponse.ok()
                                .contentType(APPLICATION_JSON)
                                .body(fromValue(response));
                        } else {
                            return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
                        }
                }).onErrorResume(err ->  ServerResponse.status(HttpStatus.UNAUTHORIZED).build());
            }
        });
    }

    public Mono<ServerResponse> getUsers(ServerRequest request) {
        int max;
        int offset;

        try {
            String maxParam = request.queryParam("max").orElse(MAX.toString());
            String offsetParam = request.queryParam("offset").orElse(OFFSET.toString());
            LOGGER.debug("Got params: [max: {}, offset: {}]", maxParam, offsetParam);
            max = Integer.parseInt(maxParam);
            offset = Integer.parseInt(offsetParam);
        } catch(NumberFormatException | NullPointerException e) {
            LOGGER.error("Params invalid, errorMsg: {}, error: {}", e.getLocalizedMessage(), e);
            max = MAX;
            offset = OFFSET;
        }

        max = Math.max(0, Math.min(max, MAX));
        offset = Math.max(OFFSET, offset);

        Flux<UserDto> users = service.get(max, offset);
        return ServerResponse.ok()
            .contentType(APPLICATION_JSON)
            .body(users, UserDto.class);
    }

    public Mono<ServerResponse> getUserById(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<UserDto> user = service.getById(id);
        return ServerResponse.ok()
            .contentType(APPLICATION_JSON)
            .body(user, UserDto.class);
    }

    public Mono<ServerResponse> getUserByEmail(ServerRequest request) {
        String email = request.queryParam("email").get();
        Mono<UserDto> user = service.getByEmail(email);
        return ServerResponse.ok()
            .contentType(APPLICATION_JSON)
            .body(user, UserDto.class);
    }

    public Mono<ServerResponse> register(ServerRequest request) {
        return request.bodyToMono(UserDto.class).flatMap(body -> {
            Set<ConstraintViolation<UserDto>> errors = validator.validate(body, New.class);
            if (!errors.isEmpty()) {
                return validateMono(errors);
            } else {
                return ServerResponse.status(HttpStatus.CREATED)
                    .contentType(APPLICATION_JSON)
                    .body(service.create(body), UserDto.class);
            }
        });
    }

    public Mono<ServerResponse> updateUser(ServerRequest request) {
        String id = request.pathVariable("id");
        return request.bodyToMono(UserDto.class)
            .flatMap(body -> {
                Set<ConstraintViolation<UserDto>> errors = validator.validate(body, Existing.class);
                if (!errors.isEmpty()) {
                    return validateMono(errors);
                } else {
                    return ServerResponse.ok()
                        .contentType(APPLICATION_JSON)
                        .body(service.update(id, body), UserDto.class);
                }
            });
    }

    public Mono<ServerResponse> deleteUser(ServerRequest request) {
        String id = request.pathVariable("id");
        return ServerResponse.ok().build(service.delete(id));
    }

    private <T> Mono<? extends ServerResponse> validateMono(Set<ConstraintViolation<T>> errors) {
        StringBuilder errorsMsgs = new StringBuilder();
        for (ConstraintViolation<?> error:errors) {
            errorsMsgs.append("Invalid value: ")
                    .append(error.getInvalidValue())
                    .append(" Error message: ")
                    .append(error.getMessage());
        }
        responseBody.put("message", errorsMsgs.toString());
        return ServerResponse.badRequest()
                .contentType(APPLICATION_JSON)
                .body(fromValue(responseBody));
    }
}
