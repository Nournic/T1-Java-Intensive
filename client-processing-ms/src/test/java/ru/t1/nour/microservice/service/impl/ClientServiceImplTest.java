package ru.t1.nour.microservice.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import ru.t1.nour.microservice.mapper.ClientMapper;
import ru.t1.nour.microservice.model.Client;
import ru.t1.nour.microservice.model.Role;
import ru.t1.nour.microservice.model.User;
import ru.t1.nour.microservice.model.dto.UserDto;
import ru.t1.nour.microservice.model.dto.request.ClientRegistrationRequest;
import ru.t1.nour.microservice.model.dto.response.ClientInfoResponse;
import ru.t1.nour.microservice.model.enums.DocumentType;
import ru.t1.nour.microservice.model.enums.RoleEnum;
import ru.t1.nour.microservice.repository.BlacklistRegistryRepository;
import ru.t1.nour.microservice.repository.ClientRepository;
import ru.t1.nour.microservice.repository.RoleRepository;
import ru.t1.nour.microservice.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceImplTest {
    @Mock
    private ClientIdGeneratorService clientIdGenerationService;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private ClientMapper clientMapper;
    @Mock
    private BlacklistRegistryRepository blacklistRegistryRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private ClientServiceImpl clientService;

    @Test
    void should_returnClientInfoResponse_when_clientExists() {
        // --- ARRANGE ---
        long clientId = 1L;
        var client = new Client(); // Создаем сущность
        var expectedResponse = new ClientInfoResponse(); // Создаем DTO для ответа

        // "Обучаем" моки
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientMapper.toClientInfoResponse(client)).thenReturn(expectedResponse);

        // --- ACT ---
        ClientInfoResponse actualResponse = clientService.findInfoById(clientId);

        // --- ASSERT ---
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse).isEqualTo(expectedResponse); // Убеждаемся, что вернулся нужный DTO
        verify(clientRepository).findById(clientId);
        verify(clientMapper).toClientInfoResponse(client);
    }

    @Test
    void should_throwException_when_findInfoByIdAndClientNotFound() {
        // --- ARRANGE ---
        long nonExistentId = 99L;
        when(clientRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> clientService.findInfoById(nonExistentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Client with ID " + nonExistentId + " is not found");

        // Убеждаемся, что маппер не вызывался
        verify(clientMapper, never()).toClientInfoResponse(any());
    }

    @Test
    void should_registerClient_when_requestIsValid() {
        // --- ARRANGE ---
        var request = new ClientRegistrationRequest(); // Заполните его валидными данными
        request.setLogin("testuser");
        request.setEmail("test@email.com");
        request.setPassword("password123");
        request.setDocumentType(DocumentType.PASSPORT);
        request.setDocumentId("123456");

        var userRole = new Role(RoleEnum.USER_ROLE);
        var savedUser = new User();
        ReflectionTestUtils.setField(savedUser, "id", 100L);
        savedUser.setLogin(request.getLogin());
        savedUser.setEmail(request.getEmail());

        String generatedClientId = "XXFF00001234";

        // Настройка поведения моков для "happy path"
        when(blacklistRegistryRepository.isCurrentlyBlacklisted(any(), any(), any())).thenReturn(false);
        when(clientRepository.existsByDocumentTypeAndDocumentId(any(), any())).thenReturn(false);
        when(userRepository.existsByLogin(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName(RoleEnum.USER_ROLE)).thenReturn(Optional.of(userRole));
        when(encoder.encode(request.getPassword())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(clientIdGenerationService.generateNext()).thenReturn(generatedClientId);

        // --- ACT ---
        UserDto result = clientService.registerClient(request);

        // --- ASSERT ---
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(savedUser.getId());
        assertThat(result.getLogin()).isEqualTo(savedUser.getLogin());

        // Проверяем, что на сохранение User ушел с правильными данными
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getPassword()).isEqualTo("hashed_password");
        assertThat(capturedUser.getRoles().toArray()).contains(userRole);

        // Проверяем, что на сохранение Client ушел с правильными данными
        ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(clientCaptor.capture());
        Client capturedClient = clientCaptor.getValue();
        assertThat(capturedClient.getClientId()).isEqualTo(generatedClientId);
        assertThat(capturedClient.getUser()).isEqualTo(savedUser);
    }

    @Test
    void should_throwException_when_registerClientAndClientIsBlacklisted() {
        var request = new ClientRegistrationRequest();
        when(blacklistRegistryRepository.isCurrentlyBlacklisted(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> clientService.registerClient(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Client is blocked in system");

        verify(clientRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void should_throwException_when_registerClientAndClientAlreadyExists() {
        var request = new ClientRegistrationRequest();
        when(blacklistRegistryRepository.isCurrentlyBlacklisted(any(), any(), any())).thenReturn(false);
        when(clientRepository.existsByDocumentTypeAndDocumentId(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> clientService.registerClient(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Client is already exists");
    }

    @Test
    void should_throwException_when_registerClientAndLoginIsTaken() {
        var request = new ClientRegistrationRequest();
        when(blacklistRegistryRepository.isCurrentlyBlacklisted(any(), any(), any())).thenReturn(false);
        when(clientRepository.existsByDocumentTypeAndDocumentId(any(), any())).thenReturn(false);
        when(userRepository.existsByLogin(any())).thenReturn(true);

        assertThatThrownBy(() -> clientService.registerClient(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Login is already taken!");
    }

    @Test
    void should_throwException_when_registerClientAndEmailIsTaken() {
        // Этот тест сразу выявляет опечатку в сообщении об ошибке в вашем коде
        var request = new ClientRegistrationRequest();
        when(blacklistRegistryRepository.isCurrentlyBlacklisted(any(), any(), any())).thenReturn(false);
        when(clientRepository.existsByDocumentTypeAndDocumentId(any(), any())).thenReturn(false);
        when(userRepository.existsByLogin(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(true);

        assertThatThrownBy(() -> clientService.registerClient(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email is already taken!");
    }

    @Test
    void should_throwException_when_registerClientAndRoleNotFound() {
        var request = new ClientRegistrationRequest();

        when(blacklistRegistryRepository.isCurrentlyBlacklisted(any(), any(), any())).thenReturn(false);
        when(clientRepository.existsByDocumentTypeAndDocumentId(any(), any())).thenReturn(false);

        // --- ИЗМЕНЕНИЕ ЗДЕСЬ ---
        // any() - совпадает с любым объектом, включая null
        when(userRepository.existsByLogin(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);

        when(roleRepository.findByName(RoleEnum.USER_ROLE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.registerClient(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Role USER_ROLE is not exists");
    }

    @Test
    void should_throwException_when_clientForFindInfoByIdNotFound() {
        // --- ARRANGE ---
        long nonExistentId = 99L;

        // "Обучаем" репозиторий возвращать "ничего"
        when(clientRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        // Проверяем, что будет выброшено исключение с правильным сообщением
        assertThatThrownBy(() -> clientService.findInfoById(nonExistentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Client with ID " + nonExistentId + " is not found");

        // Убеждаемся, что маппер не был вызван, так как до него не дошло дело
        verify(clientMapper, never()).toClientInfoResponse(any());
    }
}
