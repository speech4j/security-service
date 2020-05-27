package org.speech4j.securityservice.repository;

import org.speech4j.securityservice.domain.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, String> {

    @Query("SELECT * FROM users LIMIT :max OFFSET :offset")
    Flux<User> findAll(int max, int offset);

    @Query("SELECT * FROM users WHERE email = :email")
    Mono<User> findByEmail(String email);

    @Query("INSERT INTO users (id, email, password) VALUES (:id, :email, :password)")
    Mono<User> create(String id, String email, String password);

    @Query("UPDATE users SET email = :email, password = :password WHERE id = :id")
    Mono<User> update(String id, String email, String password);

}
