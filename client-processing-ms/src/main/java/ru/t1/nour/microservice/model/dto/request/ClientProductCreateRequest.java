package ru.t1.nour.microservice.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@AllArgsConstructor
@Builder
public class ClientProductCreateRequest {
    @NotNull
    Long clientId;

    @NotNull
    Long productId;

    @NotNull
    @DecimalMin("0.01")
    BigDecimal requestedAmount;

    @NotNull
    @Min(1)
    Integer monthCount;
}
