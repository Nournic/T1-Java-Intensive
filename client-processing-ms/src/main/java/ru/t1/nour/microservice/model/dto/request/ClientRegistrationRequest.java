package ru.t1.nour.microservice.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.t1.nour.microservice.model.enums.DocumentType;

import java.time.LocalDate;

@Data
public class ClientRegistrationRequest {
    @NotBlank
    @Size(min = 8, max = 100)
    private String login;
    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String firstName;
    private String middleName;
    @NotBlank
    private String lastName;

    @NotNull
    private LocalDate dateOfBirth;

    @NotNull
    private DocumentType documentType;

    @NotNull
    private String documentId;
    private String documentPrefix;
    private String documentSuffix;
}
