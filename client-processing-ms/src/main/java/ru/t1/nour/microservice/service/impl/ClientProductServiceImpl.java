package ru.t1.nour.microservice.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.t1.nour.microservice.mapper.ClientProductMapper;
import ru.t1.nour.microservice.model.Client;
import ru.t1.nour.microservice.model.ClientProduct;
import ru.t1.nour.microservice.model.Product;
import ru.t1.nour.microservice.model.dto.kafka.ClientProductEventDTO;
import ru.t1.nour.microservice.model.dto.request.ClientProductCreateRequest;
import ru.t1.nour.microservice.model.dto.request.ClientProductUpdateRequest;
import ru.t1.nour.microservice.model.dto.response.ClientProductResponse;
import ru.t1.nour.microservice.model.enums.Status;
import ru.t1.nour.microservice.repository.ClientProductRepository;
import ru.t1.nour.microservice.repository.ClientRepository;
import ru.t1.nour.microservice.repository.ProductRepository;
import ru.t1.nour.microservice.service.ClientProductService;
import ru.t1.nour.microservice.service.impl.kafka.ProductEventProducer;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class ClientProductServiceImpl implements ClientProductService {
    private final ClientProductRepository clientProductRepository;

    private final ClientRepository clientRepository;

    private final ProductRepository productRepository;

    private final ProductEventProducer productEventProducer;

    private final ClientProductMapper mapper;

    @Override
    @Transactional
    public ClientProductResponse create(ClientProductCreateRequest request){
        if(clientProductRepository.existsByClientIdAndProductId(request.getClientId(), request.getProductId()))
            throw new RuntimeException("ClientProduct is already exists with that clientId and productId");

        Client client = clientRepository.findById(request.getClientId()).orElseThrow(
                ()-> new RuntimeException("Client is not exists by ID " + request.getClientId())
        );

        Product product = productRepository.findById(request.getProductId()).orElseThrow(
                () -> new RuntimeException("Product is not exists by ID " + request.getProductId())
        );

        ClientProduct clientProduct = new ClientProduct();
        clientProduct.setClient(client);
        clientProduct.setProduct(product);
        clientProduct.setOpenDate(LocalDateTime.now());
        clientProduct.setStatus(Status.ACTIVE);

        ClientProduct savedProduct = clientProductRepository.save(clientProduct);

        ClientProductEventDTO event = createEvent(savedProduct, "CREATED");
        productEventProducer.sendProductEvent(event);

        return mapper.toClientProductResponse(savedProduct);
    }

    @Override
    public ClientProductResponse findById(long id) {
        ClientProduct clientProduct = clientProductRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Client product with ID " + id + "is not exists."));

        return mapper.toClientProductResponse(clientProduct);
    }

    @Override
    public Page<ClientProductResponse> findAll(Pageable pageable) {
        return clientProductRepository.findAll(pageable).map(mapper::toClientProductResponse);
    }

    @Override
    @Transactional
    public ClientProductResponse update(long id, ClientProductUpdateRequest request) {
        ClientProduct clientProduct = clientProductRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Client Product with ID " + id + "is not exists."));

        clientProduct.setStatus(request.getNewStatus());
        clientProduct.setCloseDate(request.getCloseDate());

        ClientProduct updatedProduct = clientProductRepository.save(clientProduct);

        ClientProductEventDTO event = createEvent(updatedProduct, "UPDATED");
        productEventProducer.sendProductEvent(event);

        return mapper.toClientProductResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void delete(long id) {
        ClientProduct productToDelete = clientProductRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ClientProductEventDTO event = createEvent(productToDelete, "DELETED");
        productEventProducer.sendProductEvent(event);

        clientProductRepository.deleteById(id);
    }

    private ClientProductEventDTO createEvent(ClientProduct clientProduct, String eventType){
        return new ClientProductEventDTO(
                clientProduct.getId(),
                clientProduct.getClient().getId(),
                clientProduct.getProduct().getId(),
                clientProduct.getProduct().getProductKey().toString(),
                clientProduct.getStatus().name(),
                eventType
        );
    }
}
