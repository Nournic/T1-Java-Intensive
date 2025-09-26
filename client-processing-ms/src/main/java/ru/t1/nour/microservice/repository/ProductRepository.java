package ru.t1.nour.microservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.nour.microservice.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Boolean existsById(long id);
}