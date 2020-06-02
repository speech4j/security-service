package org.speech4j.securityservice.repository;

import org.speech4j.securityservice.domain.Role;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface RoleRepository extends ReactiveCrudRepository<Role, String> {

    @Query("SELECT * FROM roles")
    Flux<Role> findAll();

    @Query("SELECT * FROM roles where id = :id")
    Mono<Role> findById(int id);

    @Query("INSERT INTO roles (name) VALUES (:name)")
    Mono<Role> create(String name);

    @Query("UPDATE roles SET name = :name WHERE id = :id")
    Mono<Role> update(int id, String name);

    @Query("DELETE FROM roles WHERE id = :id")
    Mono<Void> deleteById(int id);

    @Query(SqlQueries.GET_ROLES_BY_USER_ID)
    Flux<Role> findByUserId(String userId);

    @Query(SqlQueries.ADD_ROLE_TO_USER)
    Mono<Role> addRoleToUser(String userId, int roleId);

    @Query(SqlQueries.REMOVE_ROLE_FROM_USER)
    Mono<Void> removeRoleFromUser(String userId, int roleId);

    class SqlQueries {
        static final String GET_ROLES_BY_USER_ID = "SELECT roles.id, name " +
                "FROM roles JOIN users_roles " +
                "ON roles.id = users_roles.roles_id " +
                "WHERE users_roles.users_id = :userId";

        static final String ADD_ROLE_TO_USER = "INSERT INTO users_roles " +
                "(users_id, roles_id) VALUES (:userId, :roleId)";

        static final String REMOVE_ROLE_FROM_USER = "DELETE FROM users_roles " +
                "WHERE users_id = :userId " +
                "AND roles_id = :roleId";
    }

}
