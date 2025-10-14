package ru.t1.nour.microservice.model.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    private String username;

    @ToString.Exclude
    private String password;
}
