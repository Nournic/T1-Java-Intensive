package ru.t1.nour.microservice.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.t1.nour.microservice.model.enums.Status;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientProductUpdateRequest {
    LocalDateTime closeDate;
    Status newStatus;
}
