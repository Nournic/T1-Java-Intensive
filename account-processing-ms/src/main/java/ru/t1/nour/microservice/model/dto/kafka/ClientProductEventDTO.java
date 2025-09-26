package ru.t1.nour.microservice.model.dto.kafka;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientProductEventDTO {
    private Long clientProductId;
    private Long clientId;
    private Long productId;
    private String productKey;
    private String status;
    private BigDecimal requestedAmount;
    private Integer monthCount;
    private String eventType;
}
