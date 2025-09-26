package ru.t1.nour.microservice.service;

import ru.t1.nour.microservice.model.dto.request.CardCreateRequest;

public interface AccountService {
    void createCard(long accountId, CardCreateRequest request);
}
