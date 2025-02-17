package com.carrot.Carrot.service;

import com.carrot.Carrot.model.*;
import com.carrot.Carrot.repository.*;
import jakarta.mail.MessagingException;

import java.util.List;
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
    private final SubscriptionService subscriptionService;
    private final PendingSubscriptionRepository pendingSubscriptionRepository;
    private final PaymentService paymentService;
    private final PlanRepository planRepository;
    private final CategoriaMovimentoRepository categoriaMovimentoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MetodoPagamentoRepository metodoPagamentoRepository;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                       VerificationTokenService tokenService, EmailService emailService,
                       SubscriptionService subscriptionService, PendingSubscriptionRepository pendingSubscriptionRepository, 
                       PaymentService paymentService, PlanRepository planRepository,
                       CategoriaMovimentoRepository categoriaMovimentoRepository,
                       CategoriaRepository categoriaRepository,
                       MetodoPagamentoRepository metodoPagamentoRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.subscriptionService = subscriptionService;
        this.pendingSubscriptionRepository = pendingSubscriptionRepository;
        this.paymentService = paymentService;
        this.planRepository = planRepository;
        this.categoriaMovimentoRepository = categoriaMovimentoRepository;
        this.categoriaRepository = categoriaRepository;
        this.metodoPagamentoRepository = metodoPagamentoRepository;
    }

    public void resendEmail(User user) throws MessagingException {
        String token = tokenService.generateVerificationToken(user);
        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    public void registerUser(User user, String planName) throws MessagingException {
        // ✅ Verifica se il planId è valido PRIMA di creare l'utente
        Plan plan = null;
        if (planName != null) {
            plan = planRepository.findByName(planName).orElse(null);
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
        userRepository.save(user);

        // ✅ AGGIUNGERE QUI CATEGORIE MOVIMENTO DI DEFAULT PER UTENTE
        List<CategoriaMovimento> categorieMovimentoDefault = List.of(
            new CategoriaMovimento(null, "Entrata generica", user),
            new CategoriaMovimento(null, "Uscita generica", user)
        );
        categoriaMovimentoRepository.saveAll(categorieMovimentoDefault);

        // ✅ AGGIUNGERE QUI CATEGORIE DI DEFAULT PER UTENTE
        List<Categoria> categorieDefault = List.of(
            new Categoria(null, "Alimentari", user),
            new Categoria(null, "Trasporti", user),
            new Categoria(null, "Intrattenimento", user),
            new Categoria(null, "Salute", user)
        );
        categoriaRepository.saveAll(categorieDefault);

        // ✅ AGGIUNGERE QUI METODODIPAGAMENTO DI DEFAULT PER UTENTE
        List<MetodoPagamento> metodiPagamentoDefault = List.of(
            new MetodoPagamento(null, "Carta di Credito", user),
            new MetodoPagamento(null, "Bonifico Bancario", user),
            new MetodoPagamento(null, "Contanti", user),
            new MetodoPagamento(null, "PayPal", user)
        );
        metodoPagamentoRepository.saveAll(metodiPagamentoDefault);
            
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
