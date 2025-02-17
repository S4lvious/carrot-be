package com.carrot.Carrot.service;

import com.carrot.Carrot.model.User;
import com.carrot.Carrot.model.VerificationToken;
import com.carrot.Carrot.repository.VerificationTokenRepository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class VerificationTokenService {

    private final VerificationTokenRepository tokenRepository;
    private final EntityManager entityManager;

    public VerificationTokenService(VerificationTokenRepository tokenRepository, EntityManager entityManager) {
        this.tokenRepository = tokenRepository;
        this.entityManager = entityManager;
    }
    @Transactional
    public String generateVerificationToken(User user) {
        Optional<VerificationToken> optionalToken = tokenRepository.findByUserId(user.getId());
        VerificationToken verificationToken;
        if (optionalToken.isPresent()) {
            verificationToken = optionalToken.get();
            verificationToken.setToken(UUID.randomUUID().toString());
            verificationToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        } else {
            verificationToken = new VerificationToken(
                UUID.randomUUID().toString(),
                user,
                UUID.randomUUID().toString(),
                LocalDateTime.now().plusHours(24)
            );
        }
        return tokenRepository.save(verificationToken).getToken();
    }
    
    public VerificationToken validateToken(String token) {
        return tokenRepository.findByToken(token)
                .filter(vt -> vt.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new RuntimeException("Token non valido o scaduto!"));
    }
}
