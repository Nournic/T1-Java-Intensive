package ru.t1.nour.microservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.t1.nour.microservice.model.ClientProduct;
import ru.t1.nour.microservice.repository.ClientProductRepository;
import ru.t1.nour.microservice.service.impl.ClientProductService;
import ru.t1.nour.microservice.service.impl.kafka.ProductEventProducer;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/client-products")
@RequiredArgsConstructor
public class ClientProductResource {
    private final ClientProductService clientProductService;

    private final ClientProductRepository clientProductRepository;

    private final ObjectMapper objectMapper;

    @GetMapping
    public PagedModel<ClientProduct> getAll(Pageable pageable) {
        Page<ClientProduct> clientProducts = clientProductRepository.findAll(pageable);
        return new PagedModel<>(clientProducts);
    }

    @GetMapping("/{id}")
    public ClientProduct getOne(@PathVariable Long id) {
        Optional<ClientProduct> clientProductOptional = clientProductRepository.findById(id);
        return clientProductOptional.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));
    }

    @GetMapping("/by-ids")
    public List<ClientProduct> getMany(@RequestParam List<Long> ids) {
        return clientProductRepository.findAllById(ids);
    }

    @PostMapping
    public ClientProduct create(@RequestBody ClientProduct clientProduct) {
        return clientProductService.create(clientProduct);
    }

    @PatchMapping("/{id}")
    public ClientProduct patch(@PathVariable Long id, @RequestBody JsonNode patchNode) throws IOException {
        ClientProduct clientProduct = clientProductRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));

        objectMapper.readerForUpdating(clientProduct).readValue(patchNode);

        return clientProductRepository.save(clientProduct);
    }

    @PatchMapping
    public List<Long> patchMany(@RequestParam List<Long> ids, @RequestBody JsonNode patchNode) throws IOException {
        Collection<ClientProduct> clientProducts = clientProductRepository.findAllById(ids);

        for (ClientProduct clientProduct : clientProducts) {
            objectMapper.readerForUpdating(clientProduct).readValue(patchNode);
        }

        List<ClientProduct> resultClientProducts = clientProductRepository.saveAll(clientProducts);
        return resultClientProducts.stream()
                .map(ClientProduct::getId)
                .toList();
    }

    @DeleteMapping("/{id}")
    public ClientProduct delete(@PathVariable Long id) {
        return clientProductService.delete(id);
    }

    @DeleteMapping
    public void deleteMany(@RequestParam List<Long> ids) {
        clientProductRepository.deleteAllById(ids);
    }

    @PutMapping
    public ClientProduct update(@RequestBody ClientProduct clientProduct) {
        return clientProductService.update(clientProduct);
    }
}
