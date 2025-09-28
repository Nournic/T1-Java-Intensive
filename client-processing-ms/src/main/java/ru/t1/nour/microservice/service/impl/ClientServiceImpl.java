package ru.t1.nour.microservice.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.t1.nour.microservice.mapper.ClientMapper;
import ru.t1.nour.microservice.model.Client;
import ru.t1.nour.microservice.model.Role;
import ru.t1.nour.microservice.model.User;
import ru.t1.nour.microservice.model.dto.request.ClientRegistrationRequest;
import ru.t1.nour.microservice.model.dto.UserDto;
import ru.t1.nour.microservice.model.dto.response.ClientInfoResponse;
import ru.t1.nour.microservice.model.enums.RoleEnum;
import ru.t1.nour.microservice.repository.BlacklistRegistryRepository;
import ru.t1.nour.microservice.repository.ClientRepository;
import ru.t1.nour.microservice.repository.RoleRepository;
import ru.t1.nour.microservice.repository.UserRepository;
import ru.t1.nour.microservice.service.ClientService;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final ClientIdGeneratorService clientIdGenerationService;

    private final PasswordEncoder encoder;

    private final ClientMapper clientMapper;

    private final BlacklistRegistryRepository blacklistRegistryRepository;

    private final ClientRepository clientRepository;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public UserDto registerClient(ClientRegistrationRequest request) {
        if(blacklistRegistryRepository.isCurrentlyBlacklisted(
                request.getDocumentType(),
                request.getDocumentId(),
                LocalDateTime.now()))
            throw new RuntimeException("Client is blocked in system");

        if(clientRepository.existsByDocumentTypeAndDocumentId(
                request.getDocumentType(),
                request.getDocumentId()))
            throw new RuntimeException("Client is already exists");

        if(userRepository.existsByLogin(request.getLogin()))
            throw new RuntimeException("Login is already taken!");

        if(userRepository.existsByEmail(request.getEmail()))
            throw new RuntimeException("Login is already taken!");

        Client newClient = Client.builder()
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .documentPrefix(request.getDocumentPrefix())
                .documentSuffix(request.getDocumentSuffix())
                .documentId(request.getDocumentId())
                .build();

        User newUser = new User();
        newUser.setLogin(request.getLogin());
        newUser.setPassword(encoder.encode(request.getPassword()));
        newUser.setEmail(request.getEmail());

        Role userRole = roleRepository.findByName(RoleEnum.USER_ROLE)
                .orElseThrow(()-> new RuntimeException("Role USER_ROLE is not exists"));

        newUser.setRoles(Set.of(userRole));

        User savedUser = userRepository.save(newUser);
        newClient.setUser(savedUser);

        String newClientId = clientIdGenerationService.generateNext();
        newClient.setClientId(newClientId);

        clientRepository.save(newClient);

        return new UserDto(savedUser.getId(), savedUser.getLogin(), savedUser.getEmail());
    }

    @Override
    public ClientInfoResponse findInfoById(long id) {
        Client client = clientRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Client with ID " + id + " is not found")
        );

        return clientMapper.toClientInfoResponse(client);
    }
}
