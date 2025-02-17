package com.carrot.Carrot.service;

import com.carrot.Carrot.model.PendingSubscription;
import com.carrot.Carrot.model.User;
import com.carrot.Carrot.model.VerificationToken;
import com.carrot.Carrot.repository.PendingSubscriptionRepository;
import com.carrot.Carrot.repository.UserRepository;
import jakarta.mail.MessagingException;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenService tokenService;
    private final EmailService emailService;
    private final SubscriptionService subscriptionService; // Aggiunto SubscriptionService
    private final PendingSubscriptionRepository pendingSubscriptionRepository;
    private final PaymentService paymentService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                       VerificationTokenService tokenService, EmailService emailService,
                       SubscriptionService subscriptionService, PendingSubscriptionRepository pendingSubscriptionRepository, PaymentService paymentService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.subscriptionService = subscriptionService;
        this.pendingSubscriptionRepository = pendingSubscriptionRepository;
        this.paymentService = paymentService;
    }

    public void registerUser(User user, String planId) throws MessagingException {
        // Cripta la password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false); // L'utente deve prima verificare l'email
        userRepository.save(user);

        // Generiamo il token di verifica
        String token = tokenService.generateVerificationToken(user);

        // Inviamo l'email di verifica
        emailService.sendVerificationEmail(user.getEmail(), token);

        // Se l'utente ha scelto un piano, salviamo l'ID del piano in attesa della verifica email
        if (planId != null) {
            subscriptionService.pendingSubscription(user.getId(), planId);
        }
    }

public String verifyEmail(String token) {
    VerificationToken verificationToken = tokenService.validateToken(token);
    User user = verificationToken.getUser();

    // Abilitiamo l'utente
    user.setEnabled(true);
    userRepository.save(user);

    // Controlliamo se l'utente aveva scelto un piano
    Optional<PendingSubscription> pendingSub = pendingSubscriptionRepository.findByUserId(user.getId());

    if (pendingSub.isPresent()) {
        // L'utente ha scelto un piano, generiamo il link di pagamento
        PendingSubscription pending = pendingSub.get();
        return paymentService.createCheckoutSession(user.getId(), pending.getPlan().getName(), pending.getPlan().getPrice());
    } else {
        // Nessun piano scelto, attiviamo il periodo di prova
        subscriptionService.activateTrial(user);
        return "Email verificata con successo! Il tuo periodo di prova di 15 giorni è iniziato.";
    }
}
}
