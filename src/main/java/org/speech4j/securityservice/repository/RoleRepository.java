//package org.speech4j.securityservice.repository;
//
//import org.speech4j.securityservice.domain.Role;
//import org.springframework.data.r2dbc.repository.Query;
//import org.springframework.data.repository.reactive.ReactiveCrudRepository;
//import org.springframework.stereotype.Repository;
//import reactor.core.publisher.Mono;
//
//@Repository
//public interface RoleRepository extends ReactiveCrudRepository<Role, String> {
//
//    @Query("SELECT * FROM roles WHERE name = :name")
//    Mono<Role> findByName(String name);
//
//    @Query("INSERT INTO roles (id, name) VALUES (:id, :name)")
//    Mono<Role> create(String id, String name);
//
//    @Query("UPDATE roles SET name = :name WHERE id = :id")
//    Mono<Role> update(String id, String name);
//
//}
