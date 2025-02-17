package com.carrot.Carrot.service;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class PaymentService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    public String createCheckoutSession(Long userId, String planName, double price) {
        Stripe.apiKey = stripeSecretKey;
    
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION) // Modalità abbonamento
                .setSuccessUrl("https://app.powerwebsoftware.it/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("https://app.powerwebsoftware.it/cancel")
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("eur")
                                .setUnitAmount((long) (price * 100)) // Convertito in centesimi
                                .setRecurring( // 🔥 Indicare che il prezzo è ricorrente
                                    SessionCreateParams.LineItem.PriceData.Recurring.builder()
                                        .setInterval(SessionCreateParams.LineItem.PriceData.Recurring.Interval.MONTH) // Può essere MONTH o YEAR
                                        .build()
                                )
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(planName)
                                        .build())
                                .build())
                        .setQuantity(1L)
                        .build())
                .build();
    
        try {
            Session session = Session.create(params);
            return session.getUrl();
        } catch (Exception e) {
            throw new RuntimeException("Errore nella creazione della sessione di pagamento", e);
        }
    }
    }
