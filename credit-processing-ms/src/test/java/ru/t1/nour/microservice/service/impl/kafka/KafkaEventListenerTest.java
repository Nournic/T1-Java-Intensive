package ru.t1.nour.microservice.service.impl.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.nour.microservice.model.dto.kafka.ClientProductEventDTO;
import ru.t1.nour.microservice.model.dto.kafka.PaymentResultEventDTO;
import ru.t1.nour.microservice.service.PaymentRegistryService;
import ru.t1.nour.microservice.service.ProductRegistryService;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaEventListenerTest {

    @Mock
    private ProductRegistryService productRegistryService;
    @Mock
    private PaymentRegistryService paymentRegistryService;

    @InjectMocks
    private KafkaEventListener kafkaEventListener;

    @Test
    void should_callCreateByEvent_when_handlingCreditProductEvent() {
        // ARRANGE
        var event = new ClientProductEventDTO();
        doNothing().when(productRegistryService).createByEvent(event);

        // ACT
        kafkaEventListener.handleClientProductCreditEvent(event);

        // ASSERT
        verify(productRegistryService, times(1)).createByEvent(event);
    }

    @Test
    void should_callPerformPaymentEvent_when_handlingPaymentEvent() {
        // ARRANGE
        var event = new PaymentResultEventDTO();
        doNothing().when(paymentRegistryService).performPaymentEvent(event);

        // ACT
        kafkaEventListener.handlePaymentEvent(event);

        // ASSERT
        verify(paymentRegistryService, times(1)).performPaymentEvent(event);
    }
}
