package com.carrot.Carrot.controller;

import com.carrot.Carrot.repository.PlanRepository;
import com.carrot.Carrot.service.PaymentService;
import com.carrot.Carrot.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;
    private final PlanRepository planRepository;

    public PaymentController(PaymentService paymentService, 
                             SubscriptionService subscriptionService, 
                             PlanRepository planRepository) {
        this.paymentService = paymentService;
        this.subscriptionService = subscriptionService;
        this.planRepository = planRepository;
    }

    @PostMapping("/checkout")
    public ResponseEntity<String> createCheckoutSession(@RequestParam Long userId, 
                                                          @RequestParam String planName) {
        var planOptional = planRepository.findByName(planName);
        if (planOptional.isPresent()) {
            String planId = planOptional.get().getId();
            subscriptionService.pendingSubscription(userId, planId);
            Double price = planOptional.get().getPrice();
            String url = paymentService.createCheckoutSession(userId, planName, price);
            return ResponseEntity.ok(url);           
        } else {
            return ResponseEntity.badRequest().body("Piano non trovato");
        }
    }
}
