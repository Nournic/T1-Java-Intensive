package ru.t1.nour.microservice.integration;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.t1.nour.microservice.model.Client;
import ru.t1.nour.microservice.model.Role;
import ru.t1.nour.microservice.model.User;
import ru.t1.nour.microservice.model.dto.UserDto;
import ru.t1.nour.microservice.model.dto.request.ClientRegistrationRequest;
import ru.t1.nour.microservice.model.enums.DocumentType;
import ru.t1.nour.microservice.model.enums.RoleEnum;
import ru.t1.nour.microservice.repository.ClientRepository;
import ru.t1.nour.microservice.repository.RoleRepository;
import ru.t1.nour.microservice.repository.UserRepository;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ClientRegistrationIntegrationTest {

//    @DynamicPropertySource
//    static void dynamicProperties(DynamicPropertyRegistry registry) {
//        // --- Переменные для БД ---
//        // Эти свойства будут иметь более высокий приоритет, чем в application.properties
//        // Мы используем H2, поэтому переопределяем их
//        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
//        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
//        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.H2Dialect");
//        registry.add("spring.datasource.username", () -> "sa");
//        registry.add("spring.datasource.password", () -> "password");
//        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
//        registry.add("spring.liquibase.enabled", () -> "true");
//        registry.add("spring.sql.init.mode", () -> "never");
//        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/db.changelog-master.xml");
//
//        // --- Переменные для JWT (самое важное!) ---
//        // Генерируем "фейковые", но валидные ключи прямо здесь или копируем заранее сгенерированные
//        String fakePrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDL8spTxyieZMjOJBa8Qp8/JkpIJtizspXikUCZR2qg9kNJKd2S3FfxykUlQGdQLVDJ3tYavWcq+0jkqn80JGPNjXN/M0LltOzqyj0ja9PdEOehWo9VJl8tTjGFQx5cCfjP+xdyatOhyZenLH5wxFSmxm1la6SwU7GjziYZCbTYVtZnGGJni/2e7qz6lS7slBg5oQfUouF7jGbUk+GcpfsgSSAC5af8AzN79bfDdx9Q+qmU0KI18n0HtnEv5lnHKnRh79bucQVsAXrp1hmq/vq5bJit322Su5unXII3VEp+5bLQ+M0JqJSJSuwCumI8c2wdyCqbY6lYKqVznmfqE0KtAgMBAAECggEAAPOURZFJi3mhZTLOZ1jxOMBzOsRxoN79q4WtdTmb0f3sIPCAz+FWyLI+odab1MgtVw/C1HObQlxTQfvYpZPg4lHdXtPMpVkzfMHuSJbFcYPVbM+JGSbZbVLu0LYYM2o5frDncll0vM29tyIyQdDsqy2ET6ORrF/rlX3N+DJvJU46RC+OdyO6c7lzxIHVsQ7icI2ewR9+lXP5ID8ZrGiMfXnrBJUmiMBxSTVAgKVPx9Tx6BDLf47wdcgsrRlleFRbytexToYfW8t/u0Sva3MDAPJyOWzPl0jeHducjPfy7p1EWQCJ2XoaspiH6UP1XxdPmXw3y3M91tr04taCc5EDuQKBgQD6twsmvsIlfGQYW4UvmVzF1sOG+9Bcf8AYtEKBxTWsxexT4kan/axuCupq+7EIeYVuvqjgmm5XAxGoFTb6Z8qqYar+1gy3qyVcuFVWgtL8YceuHFfixuvoO04iYUdlHL8QC7hDdBH8Zy9G6V8mC8PTpFK1Wyhar5ImJrl8VkmOBQKBgQDQP2A1AZZ73SB2GCyCCAGHL/M5eV9tyvp/wsVnydslGIDOOFe7oJUeEVo/Qpd45KpoQBzRbe+5Z4o8oTfKxktiAl/uT7Y/K4YQkrKh+Laah9R/gPatrD0MnQ492+llBDVR9N0GU7DSk15pnOtXx3QytKPm+yAP/YqwdB/qd47aiQKBgQDR6Grnt5Ko++p6EjYCUi7AXT07SJ18tGDKzjFYoRp9WwCwgqSRhevV/kGh7LSd2dCDlhGiaumy5BjmV0y+fiPKXxdhYSkopZwUcyjP05x+PgeGmKhkcBOXTf/u/O9fkp/M+WL+5rEgV+vXPRCys+ryyYON35J7yr9cPdPSbBQNWQKBgGhBcj7XPqlgOSImVBH9RBaUEOaTNw1WczaHIiEl04qsy79yQanzaPQWp+HHyuGBSTbL8iF/MmbVyUU1SSNBRvgYTEKYsZrKhWHCr5+bTn/qxJ3YyKaF9kJcVobtk9k41CDlv8mtGHczxC6HmuLlPe0stmmrC5A11LsmR9wwYtpRAoGAM29sRfC8GU7hR7ogwfA0BOmkMj9Xq2wEPmU8CBRWX4pm024EmqG4TypBD/oNy09ZyiTqhSWom3BIgn/lzkm4tdfwdv4pZn6iQ9vlOrljVVbvozZ4t/zY4v6psteu47WGqwQN/7q31G0lEG2HTH/nb61YSAdvkDhEoAXi+uIPR+E=";
//        String fakePublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAy/LKU8conmTIziQWvEKfPyZKSCbYs7KV4pFAmUdqoPZDSSndktxX8cpFJUBnUC1Qyd7WGr1nKvtI5Kp/NCRjzY1zfzNC5bTs6so9I2vT3RDnoVqPVSZfLU4xhUMeXAn4z/sXcmrTocmXpyx+cMRUpsZtZWuksFOxo84mGQm02FbWZxhiZ4v9nu6s+pUu7JQYOaEH1KLhe4xm1JPhnKX7IEkgAuWn/AMze/W3w3cfUPqplNCiNfJ9B7ZxL+ZZxyp0Ye/W7nEFbAF66dYZqv76uWyYrd9tkrubp1yCN1RKfuWy0PjNCaiUiUrsArpiPHNsHcgqm2OpWCqlc55n6hNCrQIDAQAB";
//
//        registry.add("t1.app.security.jwt.private-key", () -> fakePrivateKey);
//
//        registry.add("t1.app.security.jwt.trusted-public-keys.c70a4596-efa6-4880-9122-b4f25cbae9bb", () -> fakePublicKey);
//        registry.add("t1.app.security.jwt.trusted-public-keys.e535ca3a-1aa1-46b2-a36e-a31e18cfc021", () -> fakePublicKey);
//        registry.add("t1.app.security.jwt.trusted-public-keys.bd7aeaa1-127f-41b9-af94-ddbfe6c61322", () -> fakePublicKey);
//        registry.add("t1.app.security.jwt.key-id", () -> "c70a4596-efa6-4880-9122-b4f25cbae9bb");
//    }


    @Autowired
    private TestRestTemplate restTemplate;

    // 4. Инжектируем репозитории для прямой проверки состояния БД
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Выполняется перед каждым тестом
    @BeforeEach
    void setUp() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users_roles", "client_products");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "clients");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users", "products");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "roles");

        Role roleToSave = new Role();
        roleToSave.setName(RoleEnum.ROLE_CURRENT_CLIENT);

        roleRepository.save(roleToSave);
    }

    @Test
    void should_registerClientAndSaveToDb_when_requestIsValid() {
        // --- ARRANGE ---
        // 1. Создаем тело запроса
        var request = new ClientRegistrationRequest();
        request.setLogin("newuserlogin");
        request.setEmail("new@email.com");
        request.setDateOfBirth(LocalDate.now());
        request.setPassword("password123");
        request.setFirstName("Иван");
        request.setLastName("Петров");
        request.setDocumentType(DocumentType.PASSPORT);
        request.setDocumentId("1234567890");

        // --- ACT ---
        // 2. Выполняем РЕАЛЬНЫЙ POST-запрос к нашему запущенному приложению
        ResponseEntity<UserDto> response = restTemplate.postForEntity(
                "/clients",
                request,
                UserDto.class
        );

        // --- ASSERT ---
        // 3. Проверяем HTTP-ответ

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getLogin()).isEqualTo("newuserlogin");

        transactionTemplate.execute(status -> {
            // 4. --- САМОЕ ГЛАВНОЕ: ПРОВЕРЯЕМ СОСТОЯНИЕ БАЗЫ ДАННЫХ ---
            // Проверяем, что пользователь был создан
            var savedUserOptional = userRepository.findByLogin("newuserlogin");
            assertThat(savedUserOptional).isPresent();
            User savedUser = savedUserOptional.get();
            assertThat(savedUser.getEmail()).isEqualTo("new@email.com");
            assertThat(savedUser.getRoles().toArray()).hasSize(1);

            Role userRole = savedUser.getRoles().iterator().next();
            assertThat(userRole.getName()).isEqualTo(RoleEnum.ROLE_CURRENT_CLIENT);

            // Проверяем, что клиент был создан и связан с пользователем
            var savedClientOptional = clientRepository.findByDocumentId("1234567890");
            assertThat(savedClientOptional).isPresent();
            Client savedClient = savedClientOptional.get();
            assertThat(savedClient.getFirstName()).isEqualTo("Иван");
            assertThat(savedClient.getUser().getId()).isEqualTo(savedUser.getId());
            return null;
        });
    }

    @Test
    void should_return4xxError_when_loginIsTaken() {
        // --- ARRANGE ---
        // 1. Сначала создаем пользователя, чтобы "занять" логин
        userRepository.save(new User("existinguser", "e@e.com", "password123"));

        // 2. Создаем запрос с тем же логином
        var request = new ClientRegistrationRequest();
        request.setLogin("existinguser");
        request.setEmail("e@e.com");
        request.setDateOfBirth(LocalDate.now());
        request.setPassword("password123");
        request.setFirstName("Иван");
        request.setLastName("Петров");
        request.setDocumentType(DocumentType.PASSPORT);
        request.setDocumentId("1234567890");

        // --- ACT ---
        ResponseEntity<Object> response = restTemplate.postForEntity(
                "/clients",
                request,
                Object.class // Нам не важен тип тела, только статус
        );

        // --- ASSERT ---
        // Проверяем, что сервер вернул ошибку клиента (4xx)
        assertThat(response.getStatusCode().is4xxClientError()).isTrue();

        // Дополнительно можно проверить, что в базе не появилось второго клиента
        assertThat(userRepository.count()).isEqualTo(1);
    }
}
