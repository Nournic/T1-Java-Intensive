package ru.t1.nour.microservice.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RoleEnum {
    USER_ROLE("USER_ROLE"),
    MODERATOR_ROLE("MODERATOR_ROLE"),
    ADMIN_ROLE("ADMIN_ROLE");

    private final String value;
}
