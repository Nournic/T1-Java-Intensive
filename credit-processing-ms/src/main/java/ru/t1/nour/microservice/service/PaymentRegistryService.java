package ru.t1.nour.microservice.service;

import ru.t1.nour.microservice.model.ProductRegistry;
import ru.t1.nour.microservice.model.dto.NextCreditPaymentDTO;
import ru.t1.nour.microservice.model.dto.kafka.PaymentResultEventDTO;

public interface PaymentRegistryService {
    void createPaymentSchedule(ProductRegistry productRegistry);

    NextCreditPaymentDTO findNextUnpaidPayment(Long clientId);

    void performPaymentEvent(PaymentResultEventDTO event);
}
