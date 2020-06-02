package org.speech4j.securityservice.service;

import org.speech4j.securityservice.dto.UserDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {

    Flux<UserDto> get(int max, int offset);

    Mono<UserDto> getById(String id);

    Mono<UserDto> getByEmail(String email);

    Mono<UserDto> getByUsername(String username);

    Mono<UserDto> create(UserDto dto);

    Mono<UserDto> update(String id, UserDto dto);

    Mono<Void> delete(String id);

}
