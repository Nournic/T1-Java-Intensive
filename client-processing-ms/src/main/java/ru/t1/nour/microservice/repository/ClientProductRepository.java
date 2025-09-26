package ru.t1.nour.microservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.nour.microservice.model.ClientProduct;

public interface ClientProductRepository extends JpaRepository<ClientProduct, Long> {
    boolean existsByClientIdAndProductId(Long clientId, Long productId);
}