package ru.t1.nour.microservice.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.t1.nour.microservice.model.dto.ClientInfoResponse;

@Component
@RequiredArgsConstructor
public class ClientApiFacade {
    private final WebClient clientProcessingWebClient;

    public ClientInfoResponse getClientInfo(Long clientId) {
        Mono<ClientInfoResponse> responseMono = clientProcessingWebClient
                .get()
                .uri("/clients/{id}/info", clientId)
                .retrieve()
                .bodyToMono(ClientInfoResponse.class);

        return responseMono.block();
    }
}
