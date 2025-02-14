package com.carrot.Carrot.controller;

import com.carrot.Carrot.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public Map<String, Object> getDashboardData() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("totaleOrdini", dashboardService.getTotaleOrdini());
        response.put("fatturatoTotale", dashboardService.getFatturatoTotale());
        response.put("fatturatoMese", dashboardService.getFatturatoMese());
        response.put("clientiAttivi", dashboardService.getClientiAttivi());
        response.put("andamentoFatturato", dashboardService.getAndamentoFatturato());
        response.put("ultimiOrdini", dashboardService.getUltimiOrdini());
        response.put("ultimeFatture", dashboardService.getUltimeFatture());
        response.put("ultimiClienti", dashboardService.getUltimiClienti());
        response.put("ultimiProdotti", dashboardService.getUltimiProdotti());
        
        return response;
    }
}
