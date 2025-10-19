package ru.t1.nour.microservice.service.controller;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.t1.nour.microservice.config.WebSecurityConfig;
import ru.t1.nour.microservice.controller.PaymentController;
import ru.t1.nour.microservice.model.dto.NextCreditPaymentDTO;
import ru.t1.nour.microservice.service.PaymentRegistryService;
import ru.t1.nour.security.jwt.AuthAccessDeniedHandler;
import ru.t1.nour.security.jwt.AuthEntryPointJwt;
import ru.t1.nour.security.jwt.JwtUtils;
import ru.t1.nour.security.jwt.properties.JwtProperties;
import ru.t1.nour.security.jwt.utils.PublicKeyLocator;
import ru.t1.nour.security.jwt.utils.TrustedKeyStore;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import({WebSecurityConfig.class, JwtUtils.class, TrustedKeyStore.class, PublicKeyLocator.class, AuthEntryPointJwt.class,
        AuthAccessDeniedHandler.class, JwtProperties.class})
public class PaymentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentRegistryService paymentRegistryService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    UsernamePasswordAuthenticationToken serviceAuth = new UsernamePasswordAuthenticationToken(
            "account-processing-ms", // Principal (имя сервиса)
            null,
            List.of(new SimpleGrantedAuthority("ROLE_SERVICE"))
    );

    @Test
    void should_return200_when_requestIsAuthenticatedAsService() throws Exception {
        // --- ARRANGE ---
        long clientId = 1L;
        when(paymentRegistryService.findNextUnpaidPayment(clientId)).thenReturn(new NextCreditPaymentDTO());

        // --- ACT & ASSERT ---
        mockMvc.perform(get("/internal/credits/next-payment")
                        .param("clientId", String.valueOf(clientId))
                        // --- МАГИЯ ЗДЕСЬ ---
                        // .with() применяет "пост-процессор" к запросу,
                        // который "подкладывает" наш объект Authentication в SecurityContext
                        .with(authentication(serviceAuth)))
                .andExpect(status().isOk());
    }

    @Test
    void should_return200AndNextPayment_when_paymentExists() throws Exception {
        // --- ARRANGE ---
        long clientId = 1L;

        // 1. Готовим DTO, который якобы вернет наш мок-сервис
        var nextPaymentDto = new NextCreditPaymentDTO();
        nextPaymentDto.setPaymentRegistryId(100L);
        nextPaymentDto.setAmount(new BigDecimal("5000.00"));
        nextPaymentDto.setPaymentDate(LocalDateTime.now().plusDays(5));

        // 2. "Обучаем" мок сервиса
        when(paymentRegistryService.findNextUnpaidPayment(clientId)).thenReturn(nextPaymentDto);

        // --- ACT & ASSERT ---
        mockMvc.perform(get("/internal/credits/next-payment")
                        .param("clientId", String.valueOf(clientId)) // Добавляем query parameter
                        .accept(MediaType.APPLICATION_JSON)
                        .with(authentication(serviceAuth)))
                // Ожидаем статус 200 OK
                .andExpect(status().isOk())
                // Проверяем, что в JSON-ответе есть нужные поля с правильными значениями
                .andExpect(jsonPath("$.paymentRegistryId").value(100L))
                .andExpect(jsonPath("$.amount").value(5000.00));
    }

    @Test
    void should_return404NotFound_when_paymentDoesNotExist() throws Exception {
        // --- ARRANGE ---
        long nonExistentClientId = 99L;

        // "Обучаем" мок сервиса выбрасывать исключение, когда платеж не найден
        when(paymentRegistryService.findNextUnpaidPayment(nonExistentClientId))
                .thenThrow(new ResourceNotFoundException("Payments are not found."));

        // --- ACT & ASSERT ---
        mockMvc.perform(get("/internal/credits/next-payment")
                        .param("clientId", String.valueOf(nonExistentClientId))
                        .accept(MediaType.APPLICATION_JSON)
                        .with(authentication(serviceAuth)))
                // Ожидаем статус 404 Not Found.
                // Это сработает, если ваше исключение ResourceNotFoundException
                // аннотировано @ResponseStatus(HttpStatus.NOT_FOUND)
                // или у вас есть @ControllerAdvice для его обработки.
                .andExpect(status().isNotFound());
    }
}
