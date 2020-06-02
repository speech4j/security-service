package org.speech4j.securityservice.handler;

import lombok.extern.slf4j.Slf4j;
import org.speech4j.securityservice.domain.Role;
import org.speech4j.securityservice.dto.RoleDto;
import org.speech4j.securityservice.dto.UserDto;
import org.speech4j.securityservice.dto.validation.Existing;
import org.speech4j.securityservice.service.RoleService;
import org.speech4j.securityservice.service.UserService;
import org.speech4j.securityservice.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class UserHandler {

    private UserService userService;
    private RoleService roleService;
    private Validator validator;
    private ValidationUtil validationUtil;

    private static final Integer MAX = 10;
    private static final Integer OFFSET = 0;

    @Autowired
    public UserHandler(UserService userService, Validator validator,
                       ValidationUtil validationUtil, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
        this.validator = validator;
        this.validationUtil = validationUtil;
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

        Flux<UserDto> users = userService.get(max, offset);
        return ServerResponse.ok()
            .contentType(APPLICATION_JSON)
            .body(users, UserDto.class);
    }

    public Mono<ServerResponse> getUserById(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<UserDto> user = userService.getById(id);
        return ServerResponse.ok()
            .contentType(APPLICATION_JSON)
            .body(user, UserDto.class);
    }

    public Mono<ServerResponse> getUserByEmail(ServerRequest request) {
        String email = request.queryParam("email").get();
        Mono<UserDto> user = userService.getByEmail(email);
        return ServerResponse.ok()
            .contentType(APPLICATION_JSON)
            .body(user, UserDto.class);
    }

    public Mono<ServerResponse> getUserByUsername(ServerRequest request) {
        String username = request.queryParam("username").get();
        Mono<UserDto> user = userService.getByEmail(username);
        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(user, UserDto.class);
    }

    public Mono<ServerResponse> updateUser(ServerRequest request) {
        String id = request.pathVariable("id");
        return request.bodyToMono(UserDto.class)
            .flatMap(body -> {
                Set<ConstraintViolation<UserDto>> errors = validator.validate(body, Existing.class);
                if (!errors.isEmpty()) {
                    return validationUtil.validateMono(errors);
                } else {
                    return ServerResponse.ok()
                        .contentType(APPLICATION_JSON)
                        .body(userService.update(id, body), UserDto.class);
                }
            });
    }

    public Mono<ServerResponse> deleteUser(ServerRequest request) {
        String id = request.pathVariable("id");
        return ServerResponse.ok().build(userService.delete(id));
    }

    public Mono<ServerResponse> getRolesByUserId(ServerRequest request) {
        String userId = request.pathVariable("id");
        Flux<RoleDto> roleDtoFlux = roleService.findByUserId(userId);
        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(roleDtoFlux, RoleDto.class);
    }

    public Mono<ServerResponse> addRoleToUser(ServerRequest request) {
        String userId = request.pathVariable("id");
        Mono<RoleDto> roleDtoMono = request.bodyToMono(RoleDto.class);
        return roleDtoMono.flatMap(roleDto ->
                roleService.addRoleToUser(userId, roleDto.getId()).flatMap(role ->
                        ServerResponse.ok().contentType(APPLICATION_JSON).body(role, RoleDto.class)
                )
        );
    }

    public Mono<ServerResponse> removeRoleFromUser(ServerRequest request) {
        String userId = request.pathVariable("userId");
        int roleId;
        try {
            roleId = Integer.parseInt(request.pathVariable("roleId"));
        } catch (Exception e) {
            LOGGER.error("Path variable parse to int error");
            return ServerResponse.badRequest().build();
        }
        return roleService.removeRoleFromUser(userId, roleId).flatMap(roleVoid ->
                ServerResponse.ok().build()
        );
    }
}
