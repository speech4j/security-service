package org.speech4j.securityservice.service;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.speech4j.securityservice.domain.Role;
import org.speech4j.securityservice.domain.User;
import org.speech4j.securityservice.dto.UserDto;
import org.speech4j.securityservice.exception.DataOperationException;
import org.speech4j.securityservice.exception.EntityExistsException;
import org.speech4j.securityservice.exception.EntityNotFoundException;
import org.speech4j.securityservice.repository.RoleRepository;
import org.speech4j.securityservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService, ReactiveUserDetailsService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private ModelMapper mapper = new ModelMapper();
    private PasswordEncoder encoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder encoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
    }

    @Override
    public Flux<UserDto> get(int max, int offset) {
        return userRepository.findAll(max, offset).map(this::mapUser);
    }

    @Override
    public Mono<UserDto> getById(String id) {
        return handleNotFound(userRepository.findById(id), id);
    }

    @Override
    public Mono<UserDto> getByEmail(String email) {
        return handleNotFound(userRepository.findByEmail(email), email);
    }

    @Override
    public Mono<UserDto> getByUsername(String username) {
        return handleNotFound(userRepository.findByUsername(username), username);
    }

    @Override
    public Mono<UserDto> create(UserDto dto) {
        dto.setId(UUID.randomUUID().toString());
        try {
            if (dto.getUsername().trim().equals("")) {
                dto.setUsername(dto.getEmail());
            }
        } catch (NullPointerException ignore) {
            dto.setUsername(dto.getEmail());
        }
        User user = mapUserDto(dto);
        user.setPassword(encoder.encode(user.getPassword()));
        LOGGER.debug("Creating user with following values: {}", user);
        return handleException(
            userRepository.create(user.getId(), user.getEmail(), user.getPassword(), user.getUsername()),
            user,
            dto
        );
    }

    @Override
    public Mono<UserDto> update(String id, UserDto dto) {
        Mono<User> existingUserMono = getById(id).map(this::mapUserDto);
        Mono<User> userMono = Mono.just(mapUserDto(dto));

        return userMono.zipWith(existingUserMono, (user, existingUser) ->
                    new User(existingUser.getId(),
                    user.getUsername(),
                    existingUser.getEmail(),
                    encoder.encode(user.getPassword()),
                    existingUser.getRoles()
            )
        ).flatMap(user -> {
            LOGGER.debug("Updating user with following values: {}", user);
            return handleException(
                userRepository.update(user.getId(), user.getUsername(), user.getPassword()),
                user,
                dto
            );
        });
    }

    @Override
    public Mono<Void> delete(String id) {
        return userRepository.deleteById(id);
    }

    private Mono<UserDto> handleNotFound(Mono<User> userMono, String field) {
        return userMono.switchIfEmpty(
            Mono.error(new EntityNotFoundException("User by field: "+field+" not found"))
        )
        .onErrorResume(err -> {
            LOGGER.error("User by field: [ {} ] not found", field);
            return Mono.error(err);
        })
        .doOnNext(user ->
            LOGGER.debug("Got user by field: [ {} ] user {}", field, user)
        ).map(this::mapUser);
    }

    private Mono<UserDto> handleException(Mono<User> userMono, User user, UserDto dto) {
        return userMono.onErrorResume(error -> {
            if (error instanceof DataIntegrityViolationException) {
                LOGGER.error("User already exists {}", dto);
                return Mono.error(new EntityExistsException("User already exists"));
            } else {
                LOGGER.error("User update failed {}", error.getLocalizedMessage());
                return Mono.error(new DataOperationException("User update failed"));
            }
        })
        .thenReturn(user)
        .map(this::mapUser);
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        Mono<User> userMono = getByUsername(username).map(this::mapUserDto);
        return userMono.flatMap(user -> {
            LOGGER.debug("Found user {}", user);
            Mono<Set<Role>> setMono = roleRepository.findByUserId(user.getId()).collect(Collectors.toSet());
            return setMono.flatMap(roles -> {
                user.setRoles(roles);
                LOGGER.debug("User with roles: {}", user);
                return Mono.just(user);
            });
        });
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
