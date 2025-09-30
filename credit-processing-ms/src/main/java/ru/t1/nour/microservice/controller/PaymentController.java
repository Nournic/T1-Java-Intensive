package ru.t1.nour.microservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.t1.nour.microservice.model.dto.NextCreditPaymentDTO;
import ru.t1.nour.microservice.service.PaymentRegistryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal")
public class PaymentController {
    private final PaymentRegistryService paymentRegistryService;

    @GetMapping("credits/next-payment")
    public ResponseEntity<NextCreditPaymentDTO> getNextPayment(@RequestParam Long clientId){
        NextCreditPaymentDTO payment = paymentRegistryService.findNextUnpaidPayment(clientId);
        return ResponseEntity
                .ok()
                .body(payment);
    }
}

