package ru.t1.nour.microservice.service;

import ru.t1.nour.microservice.model.dto.RegistrationRequest;
import ru.t1.nour.microservice.model.dto.UserDto;

public interface ClientService {
    UserDto registerClient(RegistrationRequest clientRequest);
}
