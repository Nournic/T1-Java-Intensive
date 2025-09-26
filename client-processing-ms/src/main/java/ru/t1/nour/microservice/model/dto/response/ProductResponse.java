package ru.t1.nour.microservice.model.dto.response;

import lombok.Data;
import ru.t1.nour.microservice.model.enums.ProductKey;

import java.time.LocalDateTime;

@Data
public class ProductResponse {
    protected Long id;
    protected String name;
    protected ProductKey productKey;
    protected LocalDateTime createDate;
    protected String productId;
}
