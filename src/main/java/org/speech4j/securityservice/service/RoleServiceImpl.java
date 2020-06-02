package org.speech4j.securityservice.service;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.speech4j.securityservice.domain.Role;
import org.speech4j.securityservice.dto.RoleDto;
import org.speech4j.securityservice.exception.DataOperationException;
import org.speech4j.securityservice.exception.EntityExistsException;
import org.speech4j.securityservice.exception.EntityNotFoundException;
import org.speech4j.securityservice.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class RoleServiceImpl implements RoleService {
    private RoleRepository repository;
    private ModelMapper mapper = new ModelMapper();

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.repository = roleRepository;
    }

    @Override
    public Flux<RoleDto> get() {
        return repository.findAll().map(this::mapRole);
    }

    @Override
    public Mono<RoleDto> getById(int id) {
        return handleNotFound(repository.findById(id), id + "");
    }

    @Override
    public Mono<RoleDto> create(RoleDto dto) {
        Role role = mapRoleDto(dto);
        LOGGER.debug("Creating role with following values: {}", role);
        return handleException(
                repository.create(role.getName()),
                role,
                dto
        );
    }

    @Override
    public Mono<RoleDto> update(int id, RoleDto dto) {
        Mono<Role> existingRoleMono = getById(id).map(this::mapRoleDto);
        Mono<Role> roleMono = Mono.just(mapRoleDto(dto));

        return roleMono.zipWith(existingRoleMono, (role, existingRole) ->
                new Role(existingRole.getId(),
                        role.getName()
                )
        ).flatMap(role -> {
            LOGGER.debug("Updating role with following values: {}", role);
            return handleException(
                    repository.update(role.getId(), role.getName()),
                    role,
                    dto
            );
        });
    }

    @Override
    public Mono<Void> delete(int id) {
        return repository.deleteById(id);
    }

    @Override
    public Flux<RoleDto> findByUserId(String userId) {
        return repository.findByUserId(userId).map(this::mapRole);
    }

    @Override
    public Mono<RoleDto> addRoleToUser(String userId, int roleId) {
        return repository.addRoleToUser(userId, roleId).map(this::mapRole);
    }

    @Override
    public Mono<Void> removeRoleFromUser(String userId, int roleId) {
        return repository.removeRoleFromUser(userId, roleId);
    }

    private Mono<RoleDto> handleNotFound(Mono<Role> roleMono, String field) {
        return roleMono.switchIfEmpty(
                Mono.error(new EntityNotFoundException("Role by field: "+field+" not found"))
        )
                .onErrorResume(err -> {
                    LOGGER.error("Role by field: [ {} ] not found", field);
                    return Mono.error(err);
                })
                .doOnNext(role ->
                        LOGGER.debug("Got role by field: [ {} ] user {}", field, role)
                ).map(this::mapRole);
    }

    private Mono<RoleDto> handleException(Mono<Role> roleMono, Role role, RoleDto dto) {
        return roleMono.onErrorResume(error -> {
            if (error instanceof DataIntegrityViolationException) {
                LOGGER.error("Role already exists {}", dto);
                return Mono.error(new EntityExistsException("Role already exists"));
            } else {
                LOGGER.error("Role update failed {}", error.getLocalizedMessage());
                return Mono.error(new DataOperationException("Role update failed"));
            }
        }).thenReturn(role).map(this::mapRole);
    }

    // Maps Role to RoleDto object

    private RoleDto mapRole(Role role) {
        return mapper.map(role, RoleDto.class);
    }

    // Maps RoleDto to Role object

    private Role mapRoleDto(RoleDto roleDto) {
        return mapper.map(roleDto, Role.class);
    }

}
