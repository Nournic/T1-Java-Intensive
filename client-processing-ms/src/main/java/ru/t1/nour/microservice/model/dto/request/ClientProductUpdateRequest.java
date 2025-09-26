package ru.t1.nour.microservice.model.dto.request;

import lombok.Value;
import ru.t1.nour.microservice.model.enums.Status;

import java.time.LocalDateTime;

@Value
public class ClientProductUpdateRequest {
    LocalDateTime closeDate;

    Status newStatus;
}
