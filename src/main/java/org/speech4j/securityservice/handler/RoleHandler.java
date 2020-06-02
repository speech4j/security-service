package org.speech4j.securityservice.handler;

import lombok.extern.slf4j.Slf4j;
import org.speech4j.securityservice.dto.RoleDto;
import org.speech4j.securityservice.dto.UserDto;
import org.speech4j.securityservice.dto.validation.Existing;
import org.speech4j.securityservice.dto.validation.New;
import org.speech4j.securityservice.service.RoleService;
import org.speech4j.securityservice.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
@Slf4j
public class RoleHandler {

    private RoleService service;
    private Validator validator;
    private ValidationUtil validationUtil;

    @Autowired
    public RoleHandler(RoleService service, Validator validator, ValidationUtil validationUtil) {
        this.service = service;
        this.validator = validator;
        this.validationUtil = validationUtil;
    }

    public Mono<ServerResponse> getRoles(ServerRequest request) {
        Flux<RoleDto> roles = service.get();
        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(roles, UserDto.class);
    }

    public Mono<ServerResponse> getRoleById(ServerRequest request) {
        int id;
        try {
            id = Integer.parseInt(request.pathVariable("id"));
        } catch (Exception e) {
            LOGGER.error("Path variable parse to int error");
            return ServerResponse.badRequest().build();
        }
        Mono<RoleDto> role = service.getById(id);
        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(role, UserDto.class);
    }

    public Mono<ServerResponse> createRole(ServerRequest request) {
        return request.bodyToMono(RoleDto.class).flatMap(body -> {
            Set<ConstraintViolation<RoleDto>> errors = validator.validate(body, New.class);
            if (!errors.isEmpty()) {
                return validationUtil.validateMono(errors);
            } else {
                return ServerResponse.status(HttpStatus.CREATED)
                        .contentType(APPLICATION_JSON)
                        .body(service.create(body), RoleDto.class);
            }
        });
    }


    public Mono<ServerResponse> updateRole(ServerRequest request) {
        int id = Integer.parseInt(request.pathVariable("id"));
        return request.bodyToMono(RoleDto.class)
                .flatMap(body -> {
                    Set<ConstraintViolation<RoleDto>> errors = validator.validate(body, Existing.class);
                    if (!errors.isEmpty()) {
                        return validationUtil.validateMono(errors);
                    } else {
                        return ServerResponse.ok()
                                .contentType(APPLICATION_JSON)
                                .body(service.update(id, body), RoleDto.class);
                    }
                });
    }

    public Mono<ServerResponse> deleteRole(ServerRequest request) {
        int id = Integer.parseInt(request.pathVariable("id"));
        return ServerResponse.ok().build(service.delete(id));
    }
}
