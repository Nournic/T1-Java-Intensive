package ru.t1.nour.microservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.t1.nour.microservice.repository.ProductRegistryRepository;
import ru.t1.nour.microservice.service.ProductRegistryService;

@RequiredArgsConstructor
@Service
public class ProductRegistryServiceImpl implements ProductRegistryService {
    private final ProductRegistryRepository productRegistryRepository;
}
