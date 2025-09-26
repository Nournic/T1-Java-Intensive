package ru.t1.nour.microservice.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

/**
 * DTO for {@link ru.t1.nour.microservice.model.User}
 */
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {
    Long id;
    @Size(max = 20)
    @NotBlank
    String login;
    @Size(max = 50)
    @Email
    @NotBlank
    String email;
}