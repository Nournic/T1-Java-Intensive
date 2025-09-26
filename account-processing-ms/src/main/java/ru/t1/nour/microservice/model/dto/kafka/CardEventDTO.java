package ru.t1.nour.microservice.model.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardEventDTO {
    private long accountId;
    private long clientId;
    private long productId;
    private String paymentSystem;
    private String eventType;
}
