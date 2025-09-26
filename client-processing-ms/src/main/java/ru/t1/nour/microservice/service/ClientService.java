package ru.t1.nour.microservice.service;

import ru.t1.nour.microservice.model.dto.UserDto;
import ru.t1.nour.microservice.model.dto.request.ClientRegistrationRequest;

public interface ClientService {
    UserDto registerClient(ClientRegistrationRequest clientRequest);
}
