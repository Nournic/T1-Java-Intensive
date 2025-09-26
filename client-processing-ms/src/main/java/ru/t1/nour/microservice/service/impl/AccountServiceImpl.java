package ru.t1.nour.microservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.t1.nour.microservice.model.dto.kafka.CardEventDTO;
import ru.t1.nour.microservice.model.dto.request.CardCreateRequest;
import ru.t1.nour.microservice.repository.ClientRepository;
import ru.t1.nour.microservice.repository.ProductRepository;
import ru.t1.nour.microservice.service.AccountService;
import ru.t1.nour.microservice.service.impl.kafka.CardEventProducer;

@RequiredArgsConstructor
@Service
public class AccountServiceImpl implements AccountService {
    private final CardEventProducer cardEventProducer;

    private final ClientRepository clientRepository;

    private final ProductRepository productRepository;

    @Override
    public void createCard(long accountId, CardCreateRequest request) {
        if(!clientRepository.existsById(request.getClientId()))
            throw new RuntimeException("Client with ID " + request.getClientId() + " is not exists");

        if(!productRepository.existsById(request.getProductId()))
            throw new RuntimeException("Product with ID " + request.getProductId() + " is not exists");

        CardEventDTO event = new CardEventDTO(
                accountId,
                request.getClientId(),
                request.getProductId(),
                request.getPaymentSystem(),
                "CREATE_REQUEST"
        );

        cardEventProducer.sendCardEvent(event);
    }
}
