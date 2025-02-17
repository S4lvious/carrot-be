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
        List<Plan> plans = Arrays.asList( // Usa Arrays.asList() per evitare errori con List.of()
        new Plan("Mensile", 30, 9.99),
        new Plan("Trimestrale", 90, 24.99),
        new Plan("Annuale", 365, 89.99)
    );
    
        for (Plan plan : plans) {
            if (!planRepository.existsById(plan.getId())) {
                planRepository.save(plan);
            }
        }
    }
}