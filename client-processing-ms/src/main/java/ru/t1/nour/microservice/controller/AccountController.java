package ru.t1.nour.microservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.t1.nour.microservice.model.dto.request.CardCreateRequest;
import ru.t1.nour.microservice.model.dto.response.MessageResponse;
import ru.t1.nour.microservice.service.AccountService;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/{id}/cards")
    @PreAuthorize("hasRole('CURRENT_CLIENT')")
    public ResponseEntity<?> createCard(@PathVariable long id, @Valid @RequestBody CardCreateRequest request) {
        accountService.createCard(id, request);
        return ResponseEntity
                .accepted()
                .body(new MessageResponse("The request to create a card has been submitted for processing."));
    }
}
