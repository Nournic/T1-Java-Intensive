package ru.t1.nour.microservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.nour.microservice.model.PaymentRegistry;

public interface PaymentRegistryRepository extends JpaRepository<PaymentRegistry, Long> {
}