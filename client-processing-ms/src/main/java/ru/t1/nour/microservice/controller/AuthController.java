package ru.t1.nour.microservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.t1.nour.microservice.model.dto.MessageResponse;
import ru.t1.nour.microservice.model.dto.SignUpRequest;
import ru.t1.nour.microservice.repository.UserRepository;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private UserRepository userRepository;

//    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest){
//        if (userRepository.existsByLogin(signUpRequest.getUsername())) {
//            return ResponseEntity
//                    .badRequest()
//                    .body(new MessageResponse("Error: Username is already taken!"));
//        }
//
//        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
//            return ResponseEntity
//                    .badRequest()
//                    .body(new MessageResponse("Error: Email is already in use!"));
//        }
//
//
//    }
}

