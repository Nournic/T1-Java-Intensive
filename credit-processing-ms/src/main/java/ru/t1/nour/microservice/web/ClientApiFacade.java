package ru.t1.nour.microservice.web;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
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
                .headers(headers -> headers.addAll(copyHeadersFromCurrentRequest()))
                .retrieve()
                .bodyToMono(ClientInfoResponse.class);

        return responseMono.block();
    }

    private HttpHeaders copyHeadersFromCurrentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader != null) {
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.AUTHORIZATION, authHeader);
                return headers;
            }
        }
        return new HttpHeaders();
    }
}
