package com.carrot.Carrot.repository;

import com.carrot.Carrot.model.PendingSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PendingSubscriptionRepository extends JpaRepository<PendingSubscription, UUID> {
    Optional<PendingSubscription> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
