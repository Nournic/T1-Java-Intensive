package ru.t1.nour.microservice.service.impl.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import ru.t1.nour.microservice.model.dto.kafka.ClientProductEventDTO;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductEventProducerTest {

    @Mock
    private KafkaTemplate<String, ClientProductEventDTO> kafkaTemplate;

    @InjectMocks
    private ProductEventProducer productEventProducer;

    private static final String TOPIC_CLIENT_PRODUCTS = "client_products";
    private static final String TOPIC_CLIENT_CREDIT_PRODUCTS = "client_credit_products";

    @Test
    void should_sendToStandardTopic_when_productKeyIsStandard() {
        // --- ARRANGE (Подготовка) ---

        // 1. Создаем тестовое событие со "стандартным" ключом
        ClientProductEventDTO standardEvent = new ClientProductEventDTO();
        standardEvent.setProductKey("DC"); // "DC" - это стандартный продукт

        // --- ACT (Действие) ---

        // Вызываем наш метод
        productEventProducer.sendProductEvent(standardEvent);

        // --- ASSERT (Проверка) ---

        // 1. Убеждаемся, что метод send был вызван РОВНО 1 РАЗ с ПРАВИЛЬНЫМИ аргументами
        verify(kafkaTemplate, times(1)).send(TOPIC_CLIENT_PRODUCTS, standardEvent);

        // 2. Убеждаемся, что отправки в ДРУГИЕ топики НЕ БЫЛО.
        // Это доказывает, что наша логика if/else работает правильно.
        verify(kafkaTemplate, never()).send(eq(TOPIC_CLIENT_CREDIT_PRODUCTS), any());
    }

    @Test
    void should_sendToCreditTopic_when_productKeyIsCredit() {
        // --- ARRANGE ---

        // 1. Создаем событие с "кредитным" ключом
        ClientProductEventDTO creditEvent = new ClientProductEventDTO();
        creditEvent.setProductKey("PC"); // "PC" - это кредитный продукт

        // --- ACT ---

        productEventProducer.sendProductEvent(creditEvent);

        // --- ASSERT ---

        // 1. Убеждаемся, что сообщение ушло в кредитный топик
        verify(kafkaTemplate, times(1)).send(TOPIC_CLIENT_CREDIT_PRODUCTS, creditEvent);

        // 2. Убеждаемся, что в стандартный топик оно НЕ ушло
        verify(kafkaTemplate, never()).send(eq(TOPIC_CLIENT_PRODUCTS), any());
    }

    @Test
    void should_notSendToAnyTopic_when_productKeyIsUnknown() {
        // --- ARRANGE ---

        // 1. Создаем событие с неизвестным ключом
        ClientProductEventDTO unknownEvent = new ClientProductEventDTO();
        unknownEvent.setProductKey("UNKNOWN_KEY");

        // --- ACT ---

        productEventProducer.sendProductEvent(unknownEvent);

        // --- ASSERT ---

        verify(kafkaTemplate, never()).send(anyString(), any(ClientProductEventDTO.class));
    }
}
