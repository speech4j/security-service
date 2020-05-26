package org.speech4j.securityservice.handler;

import org.speech4j.securityservice.dto.UserDto;
import org.speech4j.securityservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class UserHandler extends AbstractValidationHandler<UserDto, Validator> {

    private UserService service;

    @Autowired
    public UserHandler(UserService service, @Qualifier("webFluxValidator") Validator validator) {
        super(UserDto.class, validator);
        this.service = service;
    }

    public Mono<ServerResponse> getUsers(ServerRequest request) {
        Flux<UserDto> users = service.get();
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
        return handleRequest(request, null);
    }

    public Mono<ServerResponse> updateUser(ServerRequest request) {
        String id = request.pathVariable("id");
        return handleRequest(request, id);
    }

    public Mono<ServerResponse> deleteUser(ServerRequest request) {
        String id = request.pathVariable("id");
        return ServerResponse.ok().build(service.delete(id));
    }

    @Override
    protected Mono<ServerResponse> processBody(
            UserDto validBody, ServerRequest originalRequest, String id) {
        if (id == null) {
            return ServerResponse.status(HttpStatus.CREATED)
                .contentType(APPLICATION_JSON)
                .body(service.create(validBody), UserDto.class);
        } else {
            return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(service.update(id, validBody), UserDto.class);
        }
    }
}
