package ru.t1.nour.microservice.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.t1.nour.microservice.model.dto.NextCreditPaymentDTO;
import ru.t1.nour.security.jwt.JwtUtils;

import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CreditApiFacade {
    private final WebClient creditProcessingWebClient;
    private final JwtUtils jwtUtils;

    public Optional<NextCreditPaymentDTO> getNextPayment(Long clientId) {
        return creditProcessingWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/credits/next-payment")
                        .queryParam("clientId", clientId)
                        .build())
                .headers(headers->headers
                        .add(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtils.generateJwtToken(
                                Collections.emptyMap(), clientId.toString()
                        )))
                .retrieve()
                .onStatus(
                        status -> status.value() == HttpStatus.NOT_FOUND.value(),
                        response -> Mono.empty()
                )
                .bodyToMono(NextCreditPaymentDTO.class)
                .blockOptional();
    }
}
