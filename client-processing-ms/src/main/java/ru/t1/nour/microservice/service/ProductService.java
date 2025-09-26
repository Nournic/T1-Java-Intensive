package ru.t1.nour.microservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.t1.nour.microservice.model.dto.request.ProductCreateRequest;
import ru.t1.nour.microservice.model.dto.request.ProductUpdateRequest;
import ru.t1.nour.microservice.model.dto.response.ProductResponse;

public interface ProductService {
    ProductResponse create(ProductCreateRequest request);

    Page<ProductResponse> findAll(Pageable pageable);

    ProductResponse findById(long id);

    ProductResponse update(long id, ProductUpdateRequest request);

    void delete(long id);
}
