package ru.t1.nour.microservice.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientIdGeneratorService {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateNext() {
        // Используем нативный SQL-запрос для получения значения
        Query query = entityManager.createNativeQuery("SELECT nextval('client_id_seq')");
        Long sequenceValue = (Long) query.getSingleResult();

        String ordinalNumber = String.format("%08d", sequenceValue);
        return "77" + "01" + ordinalNumber;
    }
}
