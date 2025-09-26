package ru.t1.nour.microservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
@Configuration
public class WebClientConfig {

    @Value("services.client-processing.url")
    private String clientProcessingServiceUrl;

    @Bean
    public WebClient clientProcessingWebClient(){
        return WebClient.builder()
                .baseUrl(clientProcessingServiceUrl)
                .build();
    }
}
