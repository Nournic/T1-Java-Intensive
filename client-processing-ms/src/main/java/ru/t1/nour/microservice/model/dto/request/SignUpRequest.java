package ru.t1.nour.microservice.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {
    @NotBlank
    private String username;

    @Email
    @NotBlank
    private String email;

    @NotEmpty
    @Size(min=6, max=100)
    private String password;
}
