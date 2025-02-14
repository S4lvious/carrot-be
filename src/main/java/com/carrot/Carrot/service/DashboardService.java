package com.carrot.Carrot.service;

import com.carrot.Carrot.model.Fattura;
import com.carrot.Carrot.model.Ordine;
import com.carrot.Carrot.model.Cliente;
import com.carrot.Carrot.model.Prodotto;
import com.carrot.Carrot.repository.FatturaRepository;
import com.carrot.Carrot.repository.OrdineRepository;
import com.carrot.Carrot.repository.ClienteRepository;
import com.carrot.Carrot.repository.ProdottoRepository;
import com.carrot.Carrot.security.MyUserDetails;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final OrdineRepository ordineRepository;
    private final FatturaRepository fatturaRepository;
    private final ClienteRepository clienteRepository;
    private final ProdottoRepository prodottoRepository;

    public DashboardService(OrdineRepository ordineRepository, 
                            FatturaRepository fatturaRepository, 
                            ClienteRepository clienteRepository, 
                            ProdottoRepository prodottoRepository) {
        this.ordineRepository = ordineRepository;
        this.fatturaRepository = fatturaRepository;
        this.clienteRepository = clienteRepository;
        this.prodottoRepository = prodottoRepository;
    }

    // Metodo helper per ottenere l'ID dell'utente autenticato
    private Long getCurrentUserId() {
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder
                                        .getContext()
                                        .getAuthentication()
                                        .getPrincipal();
        return userDetails.getUser().getId();
    }

    // Restituisce il numero totale degli ordini dell'utente corrente
    public long getTotaleOrdini() {
        Long currentUserId = getCurrentUserId();
        return ordineRepository.countByUserId(currentUserId);
    }

    // Restituisce il fatturato totale (somma dei totali lordi) delle fatture dell'utente corrente
    public BigDecimal getFatturatoTotale() {
        Long currentUserId = getCurrentUserId();
        return fatturaRepository.findByUserId(currentUserId).stream()
                .map(Fattura::getTotaleLordo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Restituisce il fatturato del mese corrente (dalle fatture dell'utente corrente)
    public BigDecimal getFatturatoMese() {
        Long currentUserId = getCurrentUserId();
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        return fatturaRepository.findByUserId(currentUserId).stream()
                .filter(f -> !f.getDataEmissione().isBefore(firstDayOfMonth))
                .map(Fattura::getTotaleLordo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Restituisce il numero di clienti attivi (con ultimo ordine negli ultimi 3 mesi) per l'utente corrente
    public long getClientiAttivi() {
        Long currentUserId = getCurrentUserId();
        return clienteRepository.findByUserId(currentUserId).stream()
                .filter(c -> c.getDataUltimoOrdine() != null &&
                             c.getDataUltimoOrdine().isAfter(LocalDate.now().minusMonths(3)))
                .count();
    }

    // Restituisce i dati per il grafico dell'andamento del fatturato dell'utente corrente
    public Map<String, Object> getAndamentoFatturato() {
        Long currentUserId = getCurrentUserId();
        Map<String, BigDecimal> fatturato = fatturaRepository.findByUserId(currentUserId).stream()
                .collect(Collectors.groupingBy(
                        f -> YearMonth.from(f.getDataEmissione()).toString(),
                        Collectors.reducing(BigDecimal.ZERO, Fattura::getTotaleLordo, BigDecimal::add)
                ));

        // Ordina le etichette per data
        List<String> labels = new ArrayList<>(fatturato.keySet());
        Collections.sort(labels);

        List<BigDecimal> values = labels.stream()
                .map(fatturato::get)
                .collect(Collectors.toList());

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels", labels);
        chartData.put("datasets", List.of(Map.of(
                "label", "Fatturato",
                "data", values,
                "borderColor", "#42A5F5",
                "fill", false
        )));
        return chartData;
    }

    // Restituisce gli ultimi 5 ordini dell'utente corrente
    public List<Ordine> getUltimiOrdini() {
        Long currentUserId = getCurrentUserId();
        return ordineRepository.findTop5ByUserIdOrderByDataOrdineDesc(currentUserId);
    }

    // Restituisce le ultime 5 fatture dell'utente corrente
    public List<Fattura> getUltimeFatture() {
        Long currentUserId = getCurrentUserId();
        return fatturaRepository.findTop5ByUserIdOrderByDataEmissioneDesc(currentUserId);
    }

    // Restituisce gli ultimi 5 clienti aggiunti dall'utente corrente
    public List<Cliente> getUltimiClienti() {
        Long currentUserId = getCurrentUserId();
        return clienteRepository.findTop5ByUserIdOrderByIdDesc(currentUserId);
    }

    // Restituisce gli ultimi 5 prodotti aggiunti dall'utente corrente
    public List<Prodotto> getUltimiProdotti() {
        Long currentUserId = getCurrentUserId();
        return prodottoRepository.findTop5ByUserIdOrderByIdDesc(currentUserId);
    }
}
