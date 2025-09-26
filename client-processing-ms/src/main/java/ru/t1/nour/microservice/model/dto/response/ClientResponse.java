package ru.t1.nour.microservice.model.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import ru.t1.nour.microservice.model.Client;
import ru.t1.nour.microservice.model.dto.UserDto;
import ru.t1.nour.microservice.model.enums.DocumentType;

import java.time.LocalDate;

/**
 * DTO for {@link Client}
 */
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class ClientResponse {
    UserDto user;
    String clientId;
    String firstName;
    String middleName;
    String lastName;
    LocalDate dateOfBirth;
    DocumentType documentType;
    String documentId;
    String documentPrefix;
    String documentSuffix;
}