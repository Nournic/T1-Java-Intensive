package ru.t1.nour.microservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.t1.nour.microservice.model.dto.LoginRequest;
import ru.t1.nour.microservice.model.dto.MessageResponse;
import ru.t1.nour.microservice.model.dto.SignUpRequest;
import ru.t1.nour.microservice.service.impl.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    public ResponseEntity<?> authorize(@Valid @RequestBody LoginRequest loginRequest){
        return userService.authorize(loginRequest);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest){
        userService.registerUser(signUpRequest);
        return ResponseEntity
                .ok()
                .body(new MessageResponse("User has been created"));
    }
}

