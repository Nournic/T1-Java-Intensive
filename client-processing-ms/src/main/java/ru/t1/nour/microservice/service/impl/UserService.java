package ru.t1.nour.microservice.service.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.t1.nour.microservice.model.dto.request.AuthRequest;
import ru.t1.nour.microservice.model.dto.response.JwtResponse;
import ru.t1.nour.microservice.repository.RoleRepository;
import ru.t1.nour.microservice.repository.UserRepository;
import ru.t1.nour.security.jwt.JwtUtils;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {
    private AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final JwtUtils jwtUtils;

    private final UserDetailsServiceImpl userDetailsServiceImpl;

    private PasswordEncoder encoder;

    public ResponseEntity<?> authorize(@Valid @RequestBody AuthRequest authRequest){
        Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getUsername(), authRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(authRequest.getUsername());

        String jwtToken = jwtUtils.generateJwtToken(
                Collections.emptyMap(),
                authRequest.getUsername());

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return ResponseEntity.ok()
                .body(JwtResponse.builder()
                        .token(jwtToken)
                        .build());

    }

//    public User registerUser(SignUpRequest signUpRequest){
//        if (userRepository.existsByLogin(signUpRequest.getUsername())) {
//            throw new RuntimeException("Error: Username is already taken!");
//        }
//
//        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
//            throw new RuntimeException("Error: Email is already in use!");
//        }
//
//        User user = new User(signUpRequest.getUsername(),
//                signUpRequest.getEmail(),
//                encoder.encode(signUpRequest.getPassword()));
//
//        user.setRoles(Set.of(
//                roleRepository.findByName(RoleEnum.USER_ROLE).orElseThrow(
//                        () -> new RuntimeException("Error: Role USER_ROLE is not exist")))
//        );
//
//        userRepository.save(user);
//        return user;
//    }
}
