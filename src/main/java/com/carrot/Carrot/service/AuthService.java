package com.carrot.Carrot.service;

import com.carrot.Carrot.model.PendingSubscription;
import com.carrot.Carrot.model.Plan;
import com.carrot.Carrot.model.User;
import com.carrot.Carrot.model.VerificationToken;
import com.carrot.Carrot.repository.PendingSubscriptionRepository;
import com.carrot.Carrot.repository.PlanRepository;
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
    private final PlanRepository planRepository;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                       VerificationTokenService tokenService, EmailService emailService,
                       SubscriptionService subscriptionService, PendingSubscriptionRepository pendingSubscriptionRepository, PaymentService paymentService, PlanRepository planRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.subscriptionService = subscriptionService;
        this.pendingSubscriptionRepository = pendingSubscriptionRepository;
        this.paymentService = paymentService;
        this.planRepository = planRepository;

    }

    public void registerUser(User user, String planId) throws MessagingException {
        // ✅ Verifica se il planId è valido PRIMA di creare l'utente
        Plan plan = null;
        if (planId != null) {
            plan = planRepository.findById(planId).orElse(null);
            if (plan == null) {
                throw new IllegalArgumentException("Piano non valido");
            }
        }
    
        // ✅ Assicuriamoci che enabled e trialActive siano settati correttamente
        user.setEnabled(false); // Deve essere false fino alla verifica email
        user.setTrialActive(false); // Il trial deve essere attivato dopo il pagamento
    
        // ✅ Cripta la password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
    
        // ✅ Salva l'utente
        System.out.println("Prima del salvataggio: enabled=" + user.isEnabled() + ", trialActive=" + user.isTrialActive());
        userRepository.save(user);
        System.out.println("Dopo il salvataggio: enabled=" + userRepository.findById(user.getId()).get().isEnabled() + ", trialActive=" + userRepository.findById(user.getId()).get().isTrialActive());
            
        // ✅ Generiamo il token di verifica
        String token = tokenService.generateVerificationToken(user);
    
        // ✅ Inviamo l'email di verifica
        emailService.sendVerificationEmail(user.getEmail(), token);
    
        // ✅ Se il piano esiste, creiamo la pendingSubscription
        if (plan != null) {
            subscriptionService.pendingSubscription(user.getId(), plan.getId());
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
