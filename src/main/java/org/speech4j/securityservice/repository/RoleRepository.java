package org.speech4j.securityservice.repository;

import org.speech4j.securityservice.domain.Role;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface RoleRepository extends ReactiveCrudRepository<Role, String> {

    @Query("SELECT * FROM role where id = :id")
    Mono<Role> findById(int id);

    @Query("INSERT INTO roles (name) VALUES (:name)")
    Mono<Role> create(String name);

    @Query("UPDATE roles SET name = :name WHERE id = :id")
    Mono<Role> update(int id, String name);

    @Query("DELETE FROM roles WHERE id = :id")
    Mono<Void> deleteById(int id);

}
