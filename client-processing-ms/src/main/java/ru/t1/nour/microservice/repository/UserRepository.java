package ru.t1.nour.microservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.nour.microservice.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String username);

    Optional<User> findByLoginAndEmail(String login, String email);

    Boolean existsByLogin(String username);

    Boolean existsByEmail(String email);
}