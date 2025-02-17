package com.carrot.Carrot.service;

import com.carrot.Carrot.model.Subscription;
import com.carrot.Carrot.model.User;
import com.carrot.Carrot.model.PendingSubscription;
import com.carrot.Carrot.model.Plan;
import com.carrot.Carrot.repository.SubscriptionRepository;
import com.carrot.Carrot.repository.PendingSubscriptionRepository;
import com.carrot.Carrot.repository.PlanRepository;
import com.carrot.Carrot.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PendingSubscriptionRepository pendingSubscriptionRepository;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, 
                               PendingSubscriptionRepository pendingSubscriptionRepository,
                               PlanRepository planRepository, UserRepository userRepository, PaymentService paymentService) {
        this.subscriptionRepository = subscriptionRepository;
        this.pendingSubscriptionRepository = pendingSubscriptionRepository;
        this.planRepository = planRepository;
        this.userRepository = userRepository;
        this.paymentService = paymentService;
    }

    /**
     * Attiva il periodo di prova per un nuovo utente
     */
    public void activateTrial(User user) {
        if (subscriptionRepository.findByUserId(user.getId()).isPresent()) {
            throw new RuntimeException("L'utente ha già un abbonamento attivo.");
        }

        Subscription trialSubscription = new Subscription();
        trialSubscription.setUser(user);
        trialSubscription.setStartDate(LocalDateTime.now());
        trialSubscription.setEndDate(LocalDateTime.now().plusDays(15));
        trialSubscription.setTrial(true);

        subscriptionRepository.save(trialSubscription);
    }

    /**
     * Attiva l'abbonamento dell'utente dopo un pagamento riuscito tramite Stripe
     */
    public void activateSubscriptionAfterPayment(Long userId) {
        Optional<PendingSubscription> pendingSub = pendingSubscriptionRepository.findByUserId(userId);
    
        if (pendingSub.isPresent()) {
            PendingSubscription pending = pendingSub.get();
            
            // Usiamo direttamente il metodo subscribeToPlan per evitare duplicazione di codice
            subscribeToPlan(userId, pending.getPlan().getId());
    
            // Rimuoviamo la sottoscrizione in attesa
            pendingSubscriptionRepository.delete(pending);
        } else {
            throw new RuntimeException("Nessuna sottoscrizione in attesa trovata per l'utente con ID " + userId);
        }
    }
    
    /**
     * Memorizza un piano scelto in attesa della conferma del pagamento
     */
    public String pendingSubscription(Long userId, String planId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con ID " + userId));
    
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Piano non trovato con ID " + planId));
    
        // Salviamo la sottoscrizione in attesa di pagamento
        PendingSubscription pending = new PendingSubscription();
        pending.setUser(user);
        pending.setPlan(plan);
        pendingSubscriptionRepository.save(pending);
    
        // Generiamo il link Stripe per il pagamento
        return paymentService.createCheckoutSession(userId, plan.getName(), plan.getPrice());
    }
    

    /**
     * Controlla se l'utente ha una sottoscrizione in attesa di conferma
     */
    public boolean hasPendingSubscription(Long userId) {
        return pendingSubscriptionRepository.findByUserId(userId).isPresent();
    }

    /**
     * Verifica se un utente ha un abbonamento attivo o è in prova
     */
    public boolean hasActiveSubscription(User user) {
        Optional<Subscription> subscription = subscriptionRepository.findByUserId(user.getId());
        return subscription.isPresent() && subscription.get().getEndDate().isAfter(LocalDateTime.now());
    }

    /**
     * Acquista un nuovo piano di abbonamento
     */
    public void subscribeToPlan(Long userId, String planId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con ID " + userId));

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Piano non trovato con ID " + planId));

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(plan.getDurationDays());

        Optional<Subscription> existingSubscription = subscriptionRepository.findByUserId(userId);
        Subscription subscription;

        if (existingSubscription.isPresent()) {
            // L'utente ha già un abbonamento, aggiorniamo la scadenza
            subscription = existingSubscription.get();
            subscription.setEndDate(subscription.getEndDate().plusDays(plan.getDurationDays()));
        } else {
            // Creiamo una nuova sottoscrizione
            subscription = new Subscription();
            subscription.setUser(user);
            subscription.setPlan(plan);
            subscription.setStartDate(startDate);
            subscription.setEndDate(endDate);
            subscription.setTrial(false);
        }

        subscriptionRepository.save(subscription);
    }
}
