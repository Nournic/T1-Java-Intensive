package ru.t1.nour.microservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.t1.nour.microservice.mapper.ClientMapper;
import ru.t1.nour.microservice.model.Client;
import ru.t1.nour.microservice.model.dto.ClientDto;
import ru.t1.nour.microservice.repository.ClientRepository;
import ru.t1.nour.microservice.service.ClientService;

@RestController
@RequestMapping
@Slf4j
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;

    @PostMapping("/register")
    public ResponseEntity<Client> register(@RequestBody ClientDto dto) {
        Client resultClient = clientService.registerClient(dto);
        return ResponseEntity.ok().body(resultClient);
    }
}

