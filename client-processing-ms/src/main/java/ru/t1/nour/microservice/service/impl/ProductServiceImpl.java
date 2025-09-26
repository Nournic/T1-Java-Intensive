package ru.t1.nour.microservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.t1.nour.microservice.mapper.ProductMapper;
import ru.t1.nour.microservice.model.Product;
import ru.t1.nour.microservice.model.dto.request.ProductCreateRequest;
import ru.t1.nour.microservice.model.dto.request.ProductUpdateRequest;
import ru.t1.nour.microservice.model.dto.response.ProductResponse;
import ru.t1.nour.microservice.repository.ProductRepository;
import ru.t1.nour.microservice.service.ProductService;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {
    private final ProductMapper productMapper;

    private final ProductRepository productRepository;

    @Override
    public ProductResponse create(ProductCreateRequest request) {
        Product product = productMapper.toEntity(request);
        return productMapper.toProductResponse(productRepository.save(product));
    }

    @Override
    public Page<ProductResponse> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toProductResponse);
    }

    @Override
    public ProductResponse findById(long id) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Product with that ID is not found")
        );
        return productMapper.toProductResponse(product);
    }

    @Override
    public ProductResponse update(long id, ProductUpdateRequest request) {
        Product foundProduct = productRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Product with that ID is not found"));

        foundProduct.setName(request.getName());
        foundProduct.setKey(request.getKey());

        Product savedProduct = productRepository.save(foundProduct);

        return productMapper.toProductResponse(savedProduct);
    }

    @Override
    public void delete(long id) {
        if(!productRepository.existsById(id))
            throw new RuntimeException("Product with that ID is not found");

        productRepository.deleteById(id);
    }
}
