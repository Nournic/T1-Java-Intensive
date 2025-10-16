package ru.t1.nour.microservice.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RoleEnum {
    USER_ROLE("ROLE_CURRENT_CLIENT"),
    GRAND_EMPLOYEE_ROLE("ROLE_GRAND_EMPLOYEE"),
    MASTER_ROLE("ROLE_MASTER"),
    BLOCKED_CLIENT("ROLE_BLOCKED_CLIENT");

    private final String value;
}
