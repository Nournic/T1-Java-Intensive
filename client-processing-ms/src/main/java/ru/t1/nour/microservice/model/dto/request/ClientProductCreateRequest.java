package ru.t1.nour.microservice.model.dto.request;

import lombok.Value;

@Value
public class ClientProductCreateRequest {
    Long clientId;
    Long productId;
}
