package ru.t1.nour.microservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.nour.microservice.model.dto.UserDto;
import ru.t1.nour.microservice.model.dto.request.ClientRegistrationRequest;
import ru.t1.nour.microservice.model.dto.response.ClientInfoResponse;
import ru.t1.nour.microservice.service.ClientService;

@RestController
@RequestMapping
@Slf4j
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;

    @PostMapping("/clients")
    public ResponseEntity<UserDto> registerClient(@Valid @RequestBody ClientRegistrationRequest clientRequest) {
        UserDto resultClient = clientService.registerClient(clientRequest);
        return ResponseEntity.ok().body(resultClient);
    }

    @GetMapping("/clients/{id}/info")
    public ResponseEntity<ClientInfoResponse> getInfo(@PathVariable long id){
        ClientInfoResponse response = clientService.findInfoById(id);

        return ResponseEntity
                .ok()
                .body(response);
    }
}

