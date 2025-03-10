package com.carrot.Carrot.repository;

import com.carrot.Carrot.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlanRepository extends JpaRepository<Plan, String> {
    Optional<Plan> findByName(String name);
    boolean existsByName(String name);

}
