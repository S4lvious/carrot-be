package com.carrot.Carrot.controller;

import com.carrot.Carrot.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<String> createCheckoutSession(@RequestParam Long userId, 
                                                         @RequestParam String planName, 
                                                         @RequestParam double price) {
        String url = paymentService.createCheckoutSession(userId, planName, price);
        return ResponseEntity.ok(url);
    }
}
