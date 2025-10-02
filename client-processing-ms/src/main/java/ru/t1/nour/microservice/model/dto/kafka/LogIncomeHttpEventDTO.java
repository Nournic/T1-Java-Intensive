package ru.t1.nour.microservice.model.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogIncomeHttpEventDTO {
    private Instant timestamp;
    private String methodSignature;
    private String uri;
    private String requestMethod;
    private List<String> methodArgs;
    private String requestBody;
}
