package ru.t1.nour.microservice.service;

import ru.t1.nour.microservice.model.dto.kafka.ClientProductEventDTO;

public interface ProductRegistryService {
    void createByEvent(ClientProductEventDTO event);
}
