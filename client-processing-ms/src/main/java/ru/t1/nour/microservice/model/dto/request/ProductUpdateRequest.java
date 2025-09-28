package ru.t1.nour.microservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.t1.nour.microservice.model.enums.ProductKey;

@Data
public class ProductUpdateRequest {
    @NotNull
    String name;

    @NotBlank
    ProductKey productKey;
}
