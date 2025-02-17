package com.carrot.Carrot.repository;

import com.carrot.Carrot.model.VerificationToken;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {
    Optional<VerificationToken> findByToken(String token);
    void deleteByUserId(Long userId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<VerificationToken> findByUserId(Long userId);
}
