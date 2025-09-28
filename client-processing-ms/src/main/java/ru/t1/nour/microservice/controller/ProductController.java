package ru.t1.nour.microservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.t1.nour.microservice.model.Product;
import ru.t1.nour.microservice.model.dto.request.ProductCreateRequest;
import ru.t1.nour.microservice.model.dto.request.ProductUpdateRequest;
import ru.t1.nour.microservice.model.dto.response.MessageResponse;
import ru.t1.nour.microservice.model.dto.response.ProductResponse;
import ru.t1.nour.microservice.repository.ProductRepository;
import ru.t1.nour.microservice.service.ProductService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    private final ProductRepository productRepository;

    private final ObjectMapper objectMapper;

    @GetMapping
    public PagedModel<ProductResponse> getAll(Pageable pageable) {
        Page<ProductResponse> products = productService.findAll(pageable);
        return new PagedModel<>(products);
    }

    @GetMapping("/{id}")
    public Product getOne(@PathVariable Long id) {
        Optional<Product> productOptional = productRepository.findById(id);
        return productOptional.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));
    }

    @GetMapping("/by-ids")
    public List<Product> getMany(@RequestParam List<Long> ids) {
        return productRepository.findAllById(ids);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductCreateRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productService.create(request)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest request) {
        return ResponseEntity.ok().body(
                productService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        productService.delete(id);

        return ResponseEntity
                .ok()
                .body(new MessageResponse("Product with ID " + id + "was successfully deleted."));
    }
}
