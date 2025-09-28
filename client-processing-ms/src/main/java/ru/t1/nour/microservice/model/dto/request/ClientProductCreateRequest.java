package ru.t1.nour.microservice.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class ClientProductCreateRequest {
    @NotBlank
    Long clientId;

    @NotBlank
    Long productId;

    @NotBlank
    @DecimalMin("0.01")
    BigDecimal requestedAmount;

    @NotBlank
    @Min(1)
    Integer monthCount;
}
