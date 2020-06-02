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

    @Query("SELECT * FROM users WHERE username = :username")
    Mono<User> findByUsername(String username);

    @Query("INSERT INTO users (id, email, password, username) VALUES (:id, :email, :password, :username)")
    Mono<User> create(String id, String email, String password, String username);

    @Query("UPDATE users SET username = :username, password = :password WHERE id = :id")
    Mono<User> update(String id, String username, String password);

}
