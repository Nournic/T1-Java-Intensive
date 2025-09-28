package ru.t1.nour.microservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.t1.nour.microservice.model.enums.ProductKey;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductCreateRequest {
    @NotBlank
    String name;

    @NotBlank
    ProductKey productKey;
}
