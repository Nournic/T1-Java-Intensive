package ru.t1.nour.microservice.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RoleEnum {
    ROLE_CURRENT_CLIENT("ROLE_CURRENT_CLIENT"),
    ROLE_GRAND_EMPLOYEE("ROLE_GRAND_EMPLOYEE"),
    ROLE_MASTER("ROLE_MASTER"),
    ROLE_BLOCKED_CLIENT("ROLE_BLOCKED_CLIENT");

    private final String value;

}
