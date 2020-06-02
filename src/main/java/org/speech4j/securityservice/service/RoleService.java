package org.speech4j.securityservice.service;

import org.speech4j.securityservice.dto.RoleDto;
import org.speech4j.securityservice.dto.UserDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RoleService {

    Flux<RoleDto> get();

    Mono<RoleDto> getById(int id);

    Mono<RoleDto> create(RoleDto dto);

    Mono<RoleDto> update(int id, RoleDto dto);

    Mono<Void> delete(int id);

}
