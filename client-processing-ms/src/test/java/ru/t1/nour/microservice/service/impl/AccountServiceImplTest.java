package ru.t1.nour.microservice.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.nour.microservice.model.dto.kafka.CardEventDTO;
import ru.t1.nour.microservice.model.dto.request.CardCreateRequest;
import ru.t1.nour.microservice.repository.ClientRepository;
import ru.t1.nour.microservice.repository.ProductRepository;
import ru.t1.nour.microservice.service.impl.kafka.CardEventProducer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTest {

    @Mock
    private CardEventProducer cardEventProducer;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void should_sendCardEvent_when_clientAndProductExist(){
        // --- ARRANGE (Подготовка) ---

        // 1. Входные данные
        long accountId = 100L;
        long clientId = 1L;
        long productId = 5L;

        CardCreateRequest request = new CardCreateRequest();
        request.setClientId(clientId);
        request.setProductId(productId);
        request.setPaymentSystem("MASTERCARD");

        // 2. "Обучаем" моки репозиториев
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(productRepository.existsById(productId)).thenReturn(true);


        // --- ACT (Действие) ---

        accountService.createCard(accountId, request);


        // --- ASSERT (Проверка) ---

        // 1. Проверяем, что проверки на существование были вызваны
        verify(clientRepository, times(1)).existsById(clientId);
        verify(productRepository, times(1)).existsById(productId);

        // 2. Убеждаемся, что событие было отправлено,
        // и проверяем, с какими данными оно было сформировано.

        // Создаем "ловушку" для объекта CardEventDTO
        ArgumentCaptor<CardEventDTO> eventCaptor = ArgumentCaptor.forClass(CardEventDTO.class);

        // Проверяем, что метод sendCardEvent был вызван, и "ловим" его аргумент
        verify(cardEventProducer, times(1)).sendCardEvent(eventCaptor.capture());

        // Достаем "пойманное" событие
        CardEventDTO capturedEvent = eventCaptor.getValue();

        // Проверяем поля "пойманного" события
        assertThat(capturedEvent).isNotNull();
        assertThat(capturedEvent.getAccountId()).isEqualTo(accountId);
        assertThat(capturedEvent.getClientId()).isEqualTo(clientId);
        assertThat(capturedEvent.getProductId()).isEqualTo(productId);
        assertThat(capturedEvent.getPaymentSystem()).isEqualTo("MASTERCARD");
        assertThat(capturedEvent.getEventType()).isEqualTo("CREATE_REQUEST");
    }

    @Test
    void should_throwRuntimeException_when_clientDoesNotExist() {
        // --- ARRANGE ---
        long accountId = 100L;
        long nonExistentClientId = 99L; // Несуществующий ID
        long productId = 5L;

        CardCreateRequest request = new CardCreateRequest();
        request.setClientId(nonExistentClientId);
        request.setProductId(productId);

        // "Обучаем" мок клиента: "На проверку существования отвечай 'нет' (false)"
        when(clientRepository.existsById(nonExistentClientId)).thenReturn(false);

        // Нам даже не нужно настраивать productRepository, потому что до него
        // выполнение дойти не должно.

        // --- ACT & ASSERT ---

        // Проверяем, что вызов метода приведет к выбросу исключения с правильным сообщением
        assertThatThrownBy(() -> accountService.createCard(accountId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Client with ID " + nonExistentClientId + " is not exists");

        // --- (Дополнительная проверка) ---
        // Убеждаемся, что до проверки продукта и отправки события дело не дошло
        verify(productRepository, never()).existsById(anyLong());
        verify(cardEventProducer, never()).sendCardEvent(any(CardEventDTO.class));
    }

    @Test
    void should_throwRuntimeException_when_productDoesNotExist() {
        // --- ARRANGE ---
        long accountId = 100L;
        long clientId = 1L;
        long nonExistentProductId = 88L; // Несуществующий ID

        CardCreateRequest request = new CardCreateRequest();
        request.setClientId(clientId);
        request.setProductId(nonExistentProductId);

        // "Обучаем" моки: клиент существует, а продукт - нет.
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(productRepository.existsById(nonExistentProductId)).thenReturn(false);

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> accountService.createCard(accountId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product with ID " + nonExistentProductId + " is not exists");

        verify(cardEventProducer, never()).sendCardEvent(any(CardEventDTO.class));
        // А проверка клиента была
        verify(clientRepository, times(1)).existsById(clientId);
    }
}
