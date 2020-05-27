package org.speech4j.securityservice.handler;

import org.speech4j.securityservice.dto.UserDto;
import org.speech4j.securityservice.dto.validation.Existing;
import org.speech4j.securityservice.dto.validation.New;
import org.speech4j.securityservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

@Component
public class UserHandler {

    private UserService service;
    private Validator validator;
    private Map<String, String> responseBody;
    private static final Integer MAX = 10;
    private static final Integer OFFSET = 0;

    @Autowired
    public UserHandler(UserService service, Validator validator) {
        this.service = service;
        this.validator = validator;
        this.responseBody = new HashMap<>();
    }

    public Mono<ServerResponse> getUsers(ServerRequest request) {
        int max;
        int offset;

        try {
            max = Integer.parseInt(request.queryParam("max").orElse(MAX.toString()));
            offset = Integer.parseInt(request.queryParam("offset").orElse(OFFSET.toString()));
        } catch(NumberFormatException | NullPointerException e) {
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

    public Mono<ServerResponse> createUser(ServerRequest request) {
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

    private Mono<? extends ServerResponse> validateMono(Set<ConstraintViolation<UserDto>> errors) {
        StringBuilder errorsMsgs = new StringBuilder();
        for (ConstraintViolation<UserDto> error:errors) {
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

    private void validateParams(String max, String offset) {

    }

}
