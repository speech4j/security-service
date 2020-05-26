package org.speech4j.securityservice.service;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.speech4j.securityservice.domain.User;
import org.speech4j.securityservice.dto.UserDto;
import org.speech4j.securityservice.exception.DataOperationException;
import org.speech4j.securityservice.exception.UserExistsException;
import org.speech4j.securityservice.exception.UserNotFoundException;
import org.speech4j.securityservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private UserRepository repository;
    private ModelMapper mapper = new ModelMapper();

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.repository = userRepository;
    }

    @Override
    public Flux<UserDto> get() {
        return repository.findAll().map(this::mapUser);
    }

    @Override
    public Mono<UserDto> getById(String id) {
        return handleNotFound(repository.findById(id), id);
    }

    @Override
    public Mono<UserDto> getByEmail(String email) {
        return handleNotFound(repository.findByEmail(email), email);
    }

    @Override
    public Mono<UserDto> create(UserDto dto) {
        dto.setId(UUID.randomUUID().toString());
        User user = mapUserDto(dto);
        return handleException(
            repository.create(user.getId(), user.getEmail(), user.getPassword()),
            user,
            dto
        );
    }

    @Override
    public Mono<UserDto> update(String id, UserDto dto) {
        Mono<User> existingUserMono = getById(id).map(this::mapUserDto);
        Mono<User> userMono = Mono.just(mapUserDto(dto));

        return userMono.zipWith(existingUserMono, (user, existingUser) ->
            new User(existingUser.getId(), existingUser.getEmail(), user.getPassword())
        ).flatMap(user -> {
            LOGGER.debug("Updating with following values: {}", user);
            return handleException(
                repository.update(user.getId(), user.getEmail(), user.getPassword()),
                user,
                dto
            );
        });
    }

    @Override
    public Mono<Void> delete(String id) {
        return repository.deleteById(id);
    }

    private Mono<UserDto> handleNotFound(Mono<User> userMono, String field) {
        return userMono.switchIfEmpty(
            Mono.error(new UserNotFoundException("User by field: "+field+" not found"))
        )
        .onErrorResume(err -> {
            LOGGER.error("User by field: [ {} ] not found", field);
            return Mono.error(err);
        })
        .doOnNext(user ->
            LOGGER.debug("Got by field: [ {} ] user {}", field, user)
        ).map(this::mapUser);
    }

    private Mono<UserDto> handleException(Mono<User> userMono, User user, UserDto dto) {
        return userMono.onErrorResume(error -> {
            if (error instanceof DataIntegrityViolationException) {
                LOGGER.error("User with already exists {}", dto);
                return Mono.error(new UserExistsException("User already exists"));
            } else {
                LOGGER.error("User update failed {}", error.getLocalizedMessage());
                return Mono.error(new DataOperationException("User update failed"));
            }
        })
        .thenReturn(user)
        .map(this::mapUser);
    }

    // Maps User to UserDto object

    private UserDto mapUser(User user) {
        return mapper.map(user, UserDto.class);
    }

    // Maps UserDto to User object

    private User mapUserDto(UserDto userDto) {
        return mapper.map(userDto, User.class);
    }

}
