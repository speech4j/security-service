package org.speech4j.securityservice.handler;

import org.speech4j.securityservice.dto.UserDto;
import org.speech4j.securityservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class UserHandler {

    private UserService service;

    @Autowired
    public UserHandler(UserService service) {
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
        Mono<UserDto> userDtoMono = request.bodyToMono(UserDto.class);
        return userDtoMono.flatMap(userDto ->
                ServerResponse.status(HttpStatus.CREATED)
                        .contentType(APPLICATION_JSON)
                        .body(service.create(userDto), UserDto.class)
        );
    }

    public Mono<ServerResponse> updateUser(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<UserDto> userDtoMono = request.bodyToMono(UserDto.class);
        return userDtoMono.flatMap(userDto ->
                ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(service.update(id, userDto), UserDto.class)
        );
    }

    public Mono<ServerResponse> deleteUser(ServerRequest request) {
        String id = request.pathVariable("id");
        return ServerResponse.ok().build(service.delete(id));
    }
}
