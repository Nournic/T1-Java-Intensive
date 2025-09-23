package ru.t1.nour.microservice.model.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    @NotBlank
    private String login;

    @Email
    @NotBlank
    private String email;

    @NotEmpty
    @Size(min=6, max=100)
    private String password;
}
