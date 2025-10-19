package ru.t1.nour.microservice.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;
import ru.t1.nour.microservice.model.Client;
import ru.t1.nour.microservice.model.Role;
import ru.t1.nour.microservice.model.User;
import ru.t1.nour.microservice.model.dto.request.AuthRequest;
import ru.t1.nour.microservice.model.dto.response.ClientInfoResponse;
import ru.t1.nour.microservice.model.dto.response.JwtResponse;
import ru.t1.nour.microservice.model.enums.DocumentType;
import ru.t1.nour.microservice.model.enums.RoleEnum;
import ru.t1.nour.microservice.repository.ClientRepository;
import ru.t1.nour.microservice.repository.RoleRepository;
import ru.t1.nour.microservice.repository.UserRepository;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SecurityIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder; // Нужен для создания пользователя с хешированным паролем
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // --- Понадобится для теста защищенного ресурса ---
    @Autowired
    private ClientRepository clientRepository;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@test.com";
    private static final String TEST_PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        // Очищаем таблицы
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users_roles", "client_products");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "clients");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users", "products");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "roles");

        // Создаем роль, которую будем присваивать
        Role userRole = new Role();
        userRole.setName(RoleEnum.ROLE_CURRENT_CLIENT); // Используйте ваш Enum
        roleRepository.save(userRole);

        // Создаем тестового пользователя с хешированным паролем
        User testUser = new User();
        testUser.setLogin(TEST_USERNAME);
        testUser.setEmail(TEST_EMAIL);
        testUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        testUser.getRoles().add(userRole);
        userRepository.save(testUser);
    }

    @Test
    void should_returnJwt_when_credentialsAreValid() {
        // --- ARRANGE ---
        var loginRequest = new AuthRequest(TEST_USERNAME, TEST_PASSWORD);

        // --- ACT ---
        ResponseEntity<JwtResponse> response = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest,
                JwtResponse.class
        );

        // --- ASSERT ---
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Проверяем, что токен не пустой
        assertThat(response.getBody().getToken()).isNotBlank();
    }

    @Test
    void should_allowAccessToProtectedEndpoint_when_tokenIsValid() {
        // --- ARRANGE ---
        // Этап 1: Логинимся, чтобы получить токен
        var loginRequest = new AuthRequest(TEST_USERNAME, TEST_PASSWORD);
        ResponseEntity<JwtResponse> loginResponse = restTemplate.postForEntity("/api/auth/login", loginRequest, JwtResponse.class);
        String accessToken = loginResponse.getBody().getToken();

        // Этап 2: Готовим данные для эндпоинта (например, создадим клиента для /api/clients/{id}/info)
        User testUser = userRepository.findByLogin(TEST_USERNAME).orElseThrow();
        Client testClient = new Client();
        testClient.setDocumentType(DocumentType.PASSPORT);
        testClient.setFirstName("TEST_FIRST_NAME");
        testClient.setLastName("TEST_LAST_NAME");
        testClient.setDateOfBirth(LocalDate.now());
        testClient.setDocumentId("1234567890");
        testClient.setClientId("7701000001");
        testClient.setUser(testUser);

        testClient = clientRepository.save(testClient);

        // Этап 3: Создаем HTTP-заголовки с токеном
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // --- ACT ---
        // Выполняем GET-запрос к защищенному эндпоинту
        ResponseEntity<ClientInfoResponse> response = restTemplate.exchange(
                "/clients/{id}/info", // Пример защищенного URL
                HttpMethod.GET,
                entity,
                ClientInfoResponse.class,
                testClient.getId()
        );

        // --- ASSERT ---
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // Можно добавить проверки на содержимое ClientInfoResponse
    }

    @Test
    void should_return401Unauthorized_when_accessingProtectedEndpointWithoutToken() {

        // --- ACT ---
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/clients/1/info",
                String.class
        );

        // --- ASSERT ---
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
