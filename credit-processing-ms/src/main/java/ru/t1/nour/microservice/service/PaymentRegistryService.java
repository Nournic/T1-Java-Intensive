package ru.t1.nour.microservice.service;

import ru.t1.nour.microservice.model.ProductRegistry;

public interface PaymentRegistryService {
    void createPaymentSchedule(ProductRegistry productRegistry);
}
