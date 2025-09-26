package ru.t1.nour.microservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.t1.nour.microservice.model.dto.RegistrationRequest;
import ru.t1.nour.microservice.model.dto.UserDto;
import ru.t1.nour.microservice.service.ClientService;

@RestController
@RequestMapping
@Slf4j
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerClient(@Valid @RequestBody RegistrationRequest clientRequest) {
        UserDto resultClient = clientService.registerClient(clientRequest);
        return ResponseEntity.ok().body(resultClient);
    }
}

