package ru.t1.nour.microservice.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.t1.nour.microservice.model.dto.SignupRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest request){

    }
}

