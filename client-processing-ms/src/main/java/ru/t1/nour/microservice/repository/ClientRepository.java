package ru.t1.nour.microservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.nour.microservice.model.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {
}