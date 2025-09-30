package ru.t1.nour.microservice.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.t1.nour.microservice.model.dto.NextCreditPaymentDTO;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CreditApiFacade {
    private final WebClient creditProcessingWebClient;

    public Optional<NextCreditPaymentDTO> getNextPayment(Long clientId) {
        return creditProcessingWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/credits/next-payment")
                        .queryParam("clientId", clientId)
                        .build())
                .retrieve()
                .bodyToMono(NextCreditPaymentDTO.class)
                .blockOptional();
    }
}
