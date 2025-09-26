package ru.t1.nour.microservice.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.t1.nour.microservice.model.enums.DocumentType;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientInfoResponse {
    String firstName;
    String middleName;
    String lastName;
    LocalDate dateOfBirth;
    DocumentType documentType;
    String documentId;
    String documentPrefix;
    String documentSuffix;
}
