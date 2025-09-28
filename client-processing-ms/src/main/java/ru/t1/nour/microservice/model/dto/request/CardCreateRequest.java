package ru.t1.nour.microservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardCreateRequest {
    @NotBlank
    private long clientId;

    @NotBlank
    private long productId;

    @NotBlank
    private String paymentSystem;
}
