package ru.t1.nour.microservice.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.t1.nour.microservice.model.ClientProduct;
import ru.t1.nour.microservice.model.dto.kafka.ClientProductEventDTO;
import ru.t1.nour.microservice.repository.ClientProductRepository;
import ru.t1.nour.microservice.service.impl.kafka.ProductEventProducer;

import java.util.Objects;

@RequiredArgsConstructor
@Service
public class ClientProductService {
    private final ClientProductRepository clientProductRepository;

    private final ProductEventProducer productEventProducer;

    @Transactional
    public ClientProduct create(ClientProduct clientProduct){
        ClientProduct savedProduct = clientProductRepository.save(clientProduct);

        ClientProductEventDTO event = new ClientProductEventDTO(
                savedProduct.getId(),
                savedProduct.getClient().getId(),
                savedProduct.getProduct().getId(),
                savedProduct.getProduct().getKey().toString(),
                savedProduct.getStatus().name(),
                "CREATED"
        );
        productEventProducer.sendProductEvent(event);

        return savedProduct;
    }

    @Transactional
    public ClientProduct update(ClientProduct clientProduct) {
        if(!clientProductRepository.existsById(Objects.requireNonNull(clientProduct.getId())))
            throw new RuntimeException("Product with that id is not exists");

        ClientProduct updatedProduct = clientProductRepository.save(clientProduct);

        ClientProductEventDTO event = new ClientProductEventDTO(
                updatedProduct.getId(),
                updatedProduct.getClient().getId(),
                updatedProduct.getProduct().getId(),
                updatedProduct.getProduct().getKey().toString(),
                updatedProduct.getStatus().name(),
                "UPDATED"
        );
        productEventProducer.sendProductEvent(event);

        return updatedProduct;
    }

    @Transactional
    public ClientProduct delete(Long id) {
        ClientProduct productToDelete = clientProductRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ClientProductEventDTO event = new ClientProductEventDTO(
                productToDelete.getId(),
                productToDelete.getClient().getId(),
                productToDelete.getProduct().getId(),
                productToDelete.getProduct().getKey().toString(),
                productToDelete.getStatus().name(),
                "DELETED"
        );
        productEventProducer.sendProductEvent(event);

        clientProductRepository.deleteById(id);
        return productToDelete;
    }
}
