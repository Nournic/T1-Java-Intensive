package ru.t1.nour.microservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.t1.nour.microservice.model.dto.request.ClientProductCreateRequest;
import ru.t1.nour.microservice.model.dto.request.ClientProductUpdateRequest;
import ru.t1.nour.microservice.model.dto.response.ClientProductResponse;
import ru.t1.nour.microservice.model.dto.response.MessageResponse;
import ru.t1.nour.microservice.repository.ClientProductRepository;
import ru.t1.nour.microservice.service.ClientProductService;
import ru.t1.nour.starter.aop.annotation.HttpOutcomeRequestLog;
import ru.t1.nour.starter.aop.annotation.LogDatasourceError;

@RestController
@RequestMapping("/api/client-products")
@RequiredArgsConstructor
public class ClientProductController {
    private final ClientProductService clientProductService;

    private final ClientProductRepository clientProductRepository;

    private final ObjectMapper objectMapper;

    @HttpOutcomeRequestLog
    @GetMapping
    @PreAuthorize("hasAnyRole('GRAND_EMPLOYEE', 'MASTER')")
    public PagedModel<ClientProductResponse> getAll(Pageable pageable) {
        Page<ClientProductResponse> clientProducts = clientProductService.findAll(pageable);
        return new PagedModel<>(clientProducts);
    }

    @HttpOutcomeRequestLog
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CURRENT_CLIENT')")
    public ResponseEntity<ClientProductResponse> get(@PathVariable Long id) {
        return ResponseEntity
                .ok()
                .body(clientProductService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ClientProductResponse> create(@Valid @RequestBody ClientProductCreateRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(clientProductService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MASTER', 'GRAND_EMPLOYEE')")
    public ResponseEntity<ClientProductResponse> update(@PathVariable Long id, @Valid @RequestBody ClientProductUpdateRequest request) {
        return ResponseEntity
                .ok()
                .body(clientProductService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MASTER', 'GRAND_EMPLOYEE')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        clientProductService.delete(id);
        return ResponseEntity
                .ok()
                .body(new MessageResponse("Client Product with ID " + id + "was successfully deleted."));
    }
}
