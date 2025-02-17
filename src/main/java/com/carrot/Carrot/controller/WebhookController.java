package com.carrot.Carrot.controller;

import com.carrot.Carrot.service.SubscriptionService;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/stripe-webhook")
public class WebhookController {

    private final SubscriptionService subscriptionService;

    public WebhookController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
                                                      @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            if ("checkout.session.completed".equals(event.getType())) {
                EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();

                // Estrarre l'oggetto Session in modo sicuro
                Optional<Session> optionalSession = dataObjectDeserializer.getObject().map(Session.class::cast);

                if (optionalSession.isPresent()) {
                    Session session = optionalSession.get();

                    // Convertire l'ID utente da String a Long
                    Long userId = Long.parseLong(session.getClientReferenceId());

                    // Attivare l'abbonamento
                    subscriptionService.activateSubscriptionAfterPayment(userId);
                } else {
                    return ResponseEntity.badRequest().body("Errore nel parsing dell'evento Stripe");
                }
            }

            return ResponseEntity.ok("Webhook ricevuto con successo");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Errore nel webhook: " + e.getMessage());
        }
    }
}
