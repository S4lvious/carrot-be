package com.carrot.Carrot.security;

import com.carrot.Carrot.model.Subscription;
import com.carrot.Carrot.model.User;
import com.carrot.Carrot.service.SubscriptionService;
import com.carrot.Carrot.repository.SubscriptionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class SubscriptionInterceptor implements HandlerInterceptor {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionInterceptor(SubscriptionService subscriptionService, SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            // Cast a MyUserDetails invece di User
            MyUserDetails userDetails = (MyUserDetails) principal;
            // Supponendo che MyUserDetails contenga il modello User, per esempio:
            User user = userDetails.getUser();

            // Verifica se l'utente ha un abbonamento attivo o è ancora in prova
            Optional<Subscription> subscriptionOpt = subscriptionRepository.findByUserId(user.getId());

            if (subscriptionOpt.isPresent()) {
                Subscription subscription = subscriptionOpt.get();
                LocalDateTime now = LocalDateTime.now();

                if (subscription.getEndDate().isAfter(now)) {
                    // L'utente ha un abbonamento valido
                    return true;
                }
            }

            // Se l'abbonamento è scaduto o non esiste
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Il tuo abbonamento è scaduto. Rinnova per continuare.");
            return false;
        }

        return true;
    }
}
