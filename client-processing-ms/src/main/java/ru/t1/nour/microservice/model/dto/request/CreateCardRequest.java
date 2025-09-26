package ru.t1.nour.microservice.model.dto.request;

import ru.t1.nour.microservice.model.enums.ProductKey;

public class CreateCardRequest {
    private String clientId;
    private ProductKey productKey;
}
