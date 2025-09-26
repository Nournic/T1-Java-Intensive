package ru.t1.nour.microservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.t1.nour.microservice.model.dto.request.ClientProductCreateRequest;
import ru.t1.nour.microservice.model.dto.request.ClientProductUpdateRequest;
import ru.t1.nour.microservice.model.dto.response.ClientProductResponse;

public interface ClientProductService {
    ClientProductResponse create(ClientProductCreateRequest request);

    ClientProductResponse findById(long id);

    Page<ClientProductResponse> findAll(Pageable pageable);

    ClientProductResponse update(long id, ClientProductUpdateRequest request);

    void delete(long id);
}
