package ru.t1.nour.microservice.model.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardCreateRequest {
    private long clientId;
    private long productId;
    private String paymentSystem;
}
