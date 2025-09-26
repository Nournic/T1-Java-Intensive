package ru.t1.nour.microservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    String documentType;
    String documentId;
    String documentPrefix;
    String documentSuffix;
}
