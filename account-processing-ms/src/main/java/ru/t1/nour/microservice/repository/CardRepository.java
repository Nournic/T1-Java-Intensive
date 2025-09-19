package ru.t1.nour.microservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.nour.microservice.model.Card;

public interface CardRepository extends JpaRepository<Card, Long> {
}