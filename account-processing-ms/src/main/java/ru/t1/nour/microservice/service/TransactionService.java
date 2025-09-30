package ru.t1.nour.microservice.service;

import ru.t1.nour.microservice.model.dto.kafka.TransactionEventDTO;

import java.util.UUID;

public interface TransactionService {
    void performTransaction(TransactionEventDTO event, UUID key);
}
