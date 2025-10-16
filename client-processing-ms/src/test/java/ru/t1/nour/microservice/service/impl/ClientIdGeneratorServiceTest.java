package ru.t1.nour.microservice.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientIdGeneratorServiceTest {
    @Mock
    private EntityManager entityManager;

    @Mock
    private Query nativeQuery;

    @InjectMocks
    private ClientIdGeneratorService clientIdGeneratorService;

    @Test
    void should_generateCorrectClientId_when_sequenceReturnsValue() {
        // --- ARRANGE (Подготовка) ---

        Long sequenceValue = 123L;
        String expectedClientId = "770100000123";

        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenReturn(sequenceValue);


        // --- ACT (Действие) ---

        String actualClientId = clientIdGeneratorService.generateNext();


        // --- ASSERT (Проверка) ---

        assertThat(actualClientId).isNotNull();
        assertThat(actualClientId).isEqualTo(expectedClientId);

        verify(entityManager, times(1)).createNativeQuery("SELECT nextval('client_id_seq')");
        verify(nativeQuery, times(1)).getSingleResult();
    }

    @Test
    void should_handleLargeSequenceValue_correctly() {
        // --- ARRANGE ---
        Long largeSequenceValue = 98765432L;
        String expectedClientId = "770198765432";

        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenReturn(largeSequenceValue);

        // --- ACT ---
        String actualClientId = clientIdGeneratorService.generateNext();

        // --- ASSERT ---
        assertThat(actualClientId).isEqualTo(expectedClientId);
    }
}
