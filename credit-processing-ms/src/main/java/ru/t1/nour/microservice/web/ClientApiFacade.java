package ru.t1.nour.microservice.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.t1.nour.microservice.model.dto.ClientInfoResponse;
import ru.t1.nour.security.jwt.JwtUtils;

import java.util.Collections;


@Component
@RequiredArgsConstructor
public class ClientApiFacade {
    private final WebClient clientProcessingWebClient;
    private final JwtUtils jwtUtils;

    public ClientInfoResponse getClientInfo(Long clientId) {
        return clientProcessingWebClient
                .get()
                .uri("/clients/{id}/info", clientId)
                .headers(headers -> headers.setBearerAuth( // Используем .setBearerAuth для чистоты
                        jwtUtils.generateJwtToken(
                                Collections.emptyMap(), clientId.toString()
                        )))
                .retrieve()
                .bodyToMono(ClientInfoResponse.class)
                .block();
    }
}
