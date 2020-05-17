package org.speech4j.securityservice.service;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.speech4j.securityservice.domain.User;
import org.speech4j.securityservice.dto.UserDto;
import org.speech4j.securityservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.modelmapper.Converters.Collection.map;

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
        return repository.findById(id).doOnNext(user ->
            LOGGER.debug("Got by id {} user {}", id, user)
        ).map(this::mapUser);
    }

    @Override
    public Mono<UserDto> getByEmail(String email) {
        return repository.findByEmail(email).map(this::mapUser);
    }

    @Override
    public Mono<UserDto> create(UserDto dto) {
        User user = mapUserDto(dto);
        LOGGER.debug("Creating user ");
        return repository.create(user.getId(), user.getEmail(), user.getPassword()).doOnNext(usr ->
            LOGGER.debug("Creating userDto: {}, mapped to user: {}, results in saving user: {}", dto, user, usr)
        ).map(this::mapUser);
    }

    @Override
    public Mono<UserDto> update(String id, UserDto dto) {
        Mono<User> existingUserMono = repository.findById(id);
        Mono<User> userMono = Mono.just(mapUserDto(dto));

        return userMono.zipWith(existingUserMono, (user, existingUser) ->
                new User(existingUser.getId(), user.getEmail(), user.getPassword())
        ).flatMap(user ->
            repository.update(user.getId(), user.getEmail(), user.getPassword()).map(this::mapUser)
        );
    }

    @Override
    public Mono<Void> delete(String id) {
        return repository.deleteById(id);
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
