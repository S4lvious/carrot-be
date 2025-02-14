package com.carrot.Carrot.controller;

import com.carrot.Carrot.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    
    // Numeri: se sono null, restituisci 0 (o BigDecimal.ZERO, se usi BigDecimal)
    Long totaleOrdini = dashboardService.getTotaleOrdini();
    response.put("totaleOrdini", totaleOrdini != null ? totaleOrdini : 0);
    
    BigDecimal fatturatoTotale = dashboardService.getFatturatoTotale();
    response.put("fatturatoTotale", fatturatoTotale != null ? fatturatoTotale : BigDecimal.ZERO);
    
    BigDecimal fatturatoMese = dashboardService.getFatturatoMese();
    response.put("fatturatoMese", fatturatoMese != null ? fatturatoMese : BigDecimal.ZERO);
    
    Long clientiAttivi = dashboardService.getClientiAttivi();
    response.put("clientiAttivi", clientiAttivi != null ? clientiAttivi : 0);
    
    // Liste: se sono null, restituisci una lista vuota
    Map<String, Object> andamentoFatturato = dashboardService.getAndamentoFatturato();
    response.put("andamentoFatturato", andamentoFatturato != null ? andamentoFatturato : new ArrayList<>());
    
    List<?> ultimiOrdini = dashboardService.getUltimiOrdini();
    response.put("ultimiOrdini", ultimiOrdini != null ? ultimiOrdini : new ArrayList<>());
    
    List<?> ultimeFatture = dashboardService.getUltimeFatture();
    response.put("ultimeFatture", ultimeFatture != null ? ultimeFatture : new ArrayList<>());
    
    List<?> ultimiClienti = dashboardService.getUltimiClienti();
    response.put("ultimiClienti", ultimiClienti != null ? ultimiClienti : new ArrayList<>());
    
    List<?> ultimiProdotti = dashboardService.getUltimiProdotti();
    response.put("ultimiProdotti", ultimiProdotti != null ? ultimiProdotti : new ArrayList<>());
    
    return response;
}}
