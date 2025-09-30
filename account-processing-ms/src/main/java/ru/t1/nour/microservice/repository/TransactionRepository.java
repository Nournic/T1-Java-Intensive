package ru.t1.nour.microservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.nour.microservice.model.Transaction;

import java.time.ZonedDateTime;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    long countByCardIdAndTimestampBetween(long cardId, ZonedDateTime from, ZonedDateTime to);
}