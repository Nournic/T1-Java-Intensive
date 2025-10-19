package ru.t1.nour.microservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import ru.t1.nour.microservice.model.dto.NextCreditPaymentDTO;
import ru.t1.nour.security.jwt.JwtUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreditApiFacadeTest {
    public static MockWebServer mockWebServer;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Mock
    private JwtUtils jwtUtils;

    // --- Тестируемый класс ---
    private CreditApiFacade creditApiFacade;

    // --- Методы для запуска и остановки фейкового сервера ---
    @BeforeAll
    static void setUpServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDownServer() throws IOException {
        mockWebServer.shutdown();
    }

    // --- Метод для настройки перед каждым тестом ---
    @BeforeEach
    void setUp() {
        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());

        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();

        creditApiFacade = new CreditApiFacade(webClient, jwtUtils);
    }

    @Test
    void should_returnNextPayment_when_apiReturnsSuccess() throws Exception {
        // --- ARRANGE ---
        long clientId = 123L;
        String fakeToken = "fake.jwt.token";

        // 1. Готовим "ответ" от фейкового сервера: DTO в виде JSON-строки
        var expectedDto = new NextCreditPaymentDTO();
        expectedDto.setAmount(new BigDecimal(10));
        expectedDto.setPaymentDate(LocalDateTime.now());
        expectedDto.setExpired(false);
        expectedDto.setPaymentRegistryId(1L);
        String responseBody = objectMapper.writeValueAsString(expectedDto);

        // 2. "Программируем" фейковый сервер: "На следующий запрос ответь статусом 200 OK,
        // заголовком Content-Type и вот этим телом"
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody));

        // 3. "Обучаем" мок jwtUtils
        when(jwtUtils.generateJwtToken(Collections.emptyMap(), "123")).thenReturn(fakeToken);

        // --- ACT ---
        Optional<NextCreditPaymentDTO> actualOptional = creditApiFacade.getNextPayment(clientId);

        // --- ASSERT ---
        // 1. Проверяем, что результат не пустой
        assertThat(actualOptional).isPresent();

        // 2. САМОЕ ВАЖНОЕ: Проверяем, какой РЕАЛЬНЫЙ запрос был отправлен на сервер
        var recordedRequest = mockWebServer.takeRequest();

        // Проверяем URL
        assertThat(recordedRequest.getPath()).isEqualTo("/internal/credits/next-payment?clientId=123");
        // Проверяем HTTP-метод
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        // Проверяем заголовок Authorization
        assertThat(recordedRequest.getHeader("Authorization")).isEqualTo("Bearer " + fakeToken);
    }

    @Test
    void should_returnEmptyOptional_when_apiReturns404NotFound() throws Exception {
        // --- ARRANGE ---
        long clientId = 404L;
        String fakeToken = "fake.jwt.token";

        // "Программируем" сервер на ответ 404 Not Found
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        when(jwtUtils.generateJwtToken(any(), any())).thenReturn(fakeToken);

        // --- ACT ---
        Optional<NextCreditPaymentDTO> actualOptional = creditApiFacade.getNextPayment(clientId);

        // --- ASSERT ---
        // Убеждаемся, что .bodyToMono(...).blockOptional() правильно обработал 404
        // и вернул пустой Optional
        assertThat(actualOptional).isEmpty();

        // Проверяем, что запрос все равно был отправлен
        var recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).isEqualTo("/internal/credits/next-payment?clientId=404");
    }
}
