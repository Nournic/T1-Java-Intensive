package ru.t1.nour.microservice.service.web;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import ru.t1.nour.microservice.model.dto.ClientInfoResponse;
import ru.t1.nour.microservice.web.ClientApiFacade;
import ru.t1.nour.security.jwt.JwtUtils;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClientApiFacadeTest {
    public static MockWebServer mockWebServer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private JwtUtils jwtUtils;

    private ClientApiFacade clientApiFacade;

    @BeforeAll
    static void setUpServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDownServer() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void setUp() {
        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
        clientApiFacade = new ClientApiFacade(webClient, jwtUtils);
    }

    @Test
    void should_callCorrectEndpointWithServiceToken() throws Exception {
        // --- ARRANGE ---
        long clientId = 123L;
        String fakeServiceToken = "service-token-123";

        // 1. Готовим "ответ" от фейкового сервера
        var responseDto = new ClientInfoResponse();
        // ... (заполняем DTO)
        String responseBody = objectMapper.writeValueAsString(responseDto);

        // 2. "Программируем" сервер на успешный ответ
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody));

        // 3. "Обучаем" мок jwtUtils
        when(jwtUtils.generateJwtToken(Collections.emptyMap(), "123")).thenReturn(fakeServiceToken);

        // --- ACT ---
        ClientInfoResponse actualResponse = clientApiFacade.getClientInfo(clientId);

        // --- ASSERT ---
        // 1. Проверяем, что ответ был правильно десериализован
        assertThat(actualResponse).isNotNull();

        // 2. Проверяем, какой РЕАЛЬНЫЙ запрос был отправлен
        var recordedRequest = mockWebServer.takeRequest();

        assertThat(recordedRequest.getPath()).isEqualTo("/clients/123/info");
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getHeader("Authorization")).isEqualTo("Bearer " + fakeServiceToken);
    }
}
