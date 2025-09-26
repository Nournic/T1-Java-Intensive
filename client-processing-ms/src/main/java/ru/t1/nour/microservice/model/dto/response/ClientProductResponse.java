package ru.t1.nour.microservice.model.dto.response;

import lombok.Data;
import ru.t1.nour.microservice.model.enums.Status;

import java.time.LocalDateTime;

@Data
public class ClientProductResponse {
    private Long id;
    private ClientResponse client;
    private ProductResponse product;
    private LocalDateTime openDate;
    private LocalDateTime closeDate;
    private Status status;
}
