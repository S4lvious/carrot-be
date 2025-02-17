package com.carrot.Carrot.service;

import com.carrot.Carrot.model.User;
import com.carrot.Carrot.model.VerificationToken;
import com.carrot.Carrot.repository.VerificationTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class VerificationTokenService {

    private final VerificationTokenRepository tokenRepository;

    public VerificationTokenService(VerificationTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public String generateVerificationToken(User user) {
        // Cancella eventuali vecchi token dell'utente
        tokenRepository.deleteByUserId(user.getId());

        // Genera un nuovo token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(
                UUID.randomUUID(),
                user,
                token,
                LocalDateTime.now().plusHours(24) // Valido per 24 ore
        );
        tokenRepository.save(verificationToken);

        return token;
    }

    public VerificationToken validateToken(String token) {
        return tokenRepository.findByToken(token)
                .filter(vt -> vt.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new RuntimeException("Token non valido o scaduto!"));
    }
}
