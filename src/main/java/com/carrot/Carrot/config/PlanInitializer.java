package com.carrot.Carrot.config;

import com.carrot.Carrot.model.Plan;
import com.carrot.Carrot.repository.PlanRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class PlanInitializer implements CommandLineRunner {

    private final PlanRepository planRepository;

    public PlanInitializer(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    public void run(String... args) {
        if (planRepository.count() == 0) {
            List<Plan> plans = Arrays.asList(
                    new Plan(UUID.randomUUID(), "Mensile", 30, 9.99),
                    new Plan(UUID.randomUUID(), "Trimestrale", 90, 24.99),
                    new Plan(UUID.randomUUID(), "Semestrale", 180, 44.99),
                    new Plan(UUID.randomUUID(), "Annuale", 365, 79.99)
            );
            planRepository.saveAll(plans);
        }
    }
}
