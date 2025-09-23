package ru.t1.nour.microservice.service;

import ru.t1.nour.microservice.model.Client;
import ru.t1.nour.microservice.model.dto.ClientDto;

public interface ClientService {
    Client registerClient(ClientDto dto);
}
