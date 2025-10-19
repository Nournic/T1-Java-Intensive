package ru.t1.nour.microservice.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.jdbc.JdbcTestUtils;
import ru.t1.nour.microservice.model.*;
import ru.t1.nour.microservice.model.dto.kafka.ClientProductEventDTO;
import ru.t1.nour.microservice.model.dto.request.AuthRequest;
import ru.t1.nour.microservice.model.dto.request.ClientProductCreateRequest;
import ru.t1.nour.microservice.model.dto.response.ClientProductResponse;
import ru.t1.nour.microservice.model.dto.response.JwtResponse;
import ru.t1.nour.microservice.model.enums.DocumentType;
import ru.t1.nour.microservice.model.enums.ProductKey;
import ru.t1.nour.microservice.model.enums.RoleEnum;
import ru.t1.nour.microservice.repository.*;
import ru.t1.nour.microservice.service.impl.kafka.ProductEventProducer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ClientProductCreationIntegrationTest {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ClientProductRepository clientProductRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private ProductEventProducer productEventProducer;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private long testClientPrimaryId;
    private long testProductPrimaryId;

    @BeforeEach
    void setUp() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users_roles", "client_products");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "clients");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users", "products");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "roles");

        // 1. Создаем роль MASTER
        Role masterRole = new Role();
        masterRole.setName(RoleEnum.ROLE_MASTER);
        roleRepository.save(masterRole);

        // 2. Создаем пользователя с этой ролью
        User masterUser = new User("master", "master@test.com", passwordEncoder.encode("password"));
        masterUser.getRoles().add(masterRole);
        userRepository.save(masterUser);

        // 3. Создаем клиента
        Client client = new Client();
        client.setClientId("7701000001");
        client.setUser(masterUser);
        client.setFirstName("TEST_FIRST_NAME");
        client.setLastName("TEST_LAST_NAME");
        client.setDocumentType(DocumentType.PASSPORT);
        client.setDocumentId("1234567890");
        client.setDateOfBirth(LocalDate.now());
        Client savedClient = clientRepository.save(client);
        testClientPrimaryId = savedClient.getId();

        // 4. Создаём продукт
        Product product = new Product();
        product.setName("Дебетовая карта \"Классика\"");
        product.setProductKey(ProductKey.DC);
        product.setCreateDate(LocalDateTime.now());
        Product savedProduct = productRepository.save(product);
        this.testProductPrimaryId = savedProduct.getId();
    }

    @Test
    void should_createClientProduct_when_userIsMaster() {
        // --- ARRANGE ---
        // 1. Логинимся под пользователем-мастером, чтобы получить токен
        var loginRequest = new AuthRequest("master", "password");
        ResponseEntity<JwtResponse> loginResponse = restTemplate.postForEntity("/api/auth/login", loginRequest, JwtResponse.class);
        String masterToken = loginResponse.getBody().getToken();

        // 2. Готовим тело запроса
        var createRequest = new ClientProductCreateRequest(
                testClientPrimaryId,
                testProductPrimaryId,
                BigDecimal.ONE,
                12

        );

        // 3. Готовим заголовки
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(masterToken);
        HttpEntity<ClientProductCreateRequest> entity = new HttpEntity<>(createRequest, headers);

        // --- ACT ---
        ResponseEntity<ClientProductResponse> response = restTemplate.postForEntity(
                "/api/client-products", // Ваш URL для создания
                entity,
                ClientProductResponse.class
        );


        // --- ASSERT ---
        // 1. Проверяем HTTP-ответ
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // 2. Ловим событие от Kafka
        ArgumentCaptor<ClientProductEventDTO> eventCaptor = ArgumentCaptor.forClass(ClientProductEventDTO.class);
        verify(productEventProducer, timeout(1000).times(1)).sendProductEvent(eventCaptor.capture());
        ClientProductEventDTO sentEvent = eventCaptor.getValue();
        assertThat(sentEvent.getEventType()).isEqualTo("CREATED");


        // 3. Проверяем, что в базе данных появилась новая запись
        assertThat(clientProductRepository.count()).isEqualTo(1);
        ClientProduct savedProduct = clientProductRepository.findAll().get(0);
        assertThat(savedProduct.getClient().getId()).isEqualTo(testClientPrimaryId);
    }

    @Test
    void should_return403Forbidden_when_creatingClientProductWithInsufficientRole() {
        // --- ARRANGE ---

        // 1. ВМЕСТО masterUser создаем обычного пользователя.
        // Сначала нужно создать для него роль в базе.
        Role clientRole = new Role();
        clientRole.setName(RoleEnum.ROLE_CURRENT_CLIENT);
        roleRepository.save(clientRole);

        User normalUser = new User("testclient", "client@test.com", passwordEncoder.encode("password"));
        normalUser.getRoles().add(clientRole);
        User savedUser = userRepository.save(normalUser);

        // 2. Логинимся под этим ОБЫЧНЫМ пользователем, чтобы получить его токен
        var loginRequest = new AuthRequest("testclient", "password");
        ResponseEntity<JwtResponse> loginResponse = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest,
                JwtResponse.class
        );
        String clientToken = loginResponse.getBody().getToken();

        // 3. Готовим тело запроса (ID клиента и продукта из setUp() все еще валидны)
        var createRequest = ClientProductCreateRequest.builder()
                .clientId(this.testClientPrimaryId)
                .productId(this.testProductPrimaryId)
                .requestedAmount(BigDecimal.ONE)
                .monthCount(12)
                .build();

        // 4. Готовим заголовки с токеном БЕЗ нужных прав
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(clientToken);
        HttpEntity<ClientProductCreateRequest> entity = new HttpEntity<>(createRequest, headers);

        // --- ACT ---
        // Выполняем POST-запрос к эндпоинту, который требует ROLE_MASTER
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/client-products",
                entity,
                String.class
        );

        // --- ASSERT ---
        // 1. Убеждаемся, что получили отказ в доступе
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // 2. Убеждаемся, что в базу ничего не было записано
        assertThat(clientProductRepository.count()).isEqualTo(0);
    }
}
