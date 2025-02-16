package com.carrot.Carrot.service;

import com.carrot.Carrot.enumerator.TipoMovimento;
import com.carrot.Carrot.model.PrimaNota;
import com.carrot.Carrot.repository.PrimaNotaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PrimaNotaService {

    @Autowired
    private PrimaNotaRepository primaNotaRepository;

    // Ottenere tutte le operazioni dell'utente
    public List<PrimaNota> getAllByUser(Long userId) {
        return primaNotaRepository.findByUserId(userId);
    }

    // Ottenere una singola operazione
    public Optional<PrimaNota> getById(Long id) {
        return primaNotaRepository.findById(id);
    }

    // Ottenere solo le ENTRATE o le USCITE
    public List<PrimaNota> getByTipo(Long userId, TipoMovimento tipoMovimento) {
        return primaNotaRepository.findByUserIdAndTipoMovimento(userId, tipoMovimento);
    }

    // Creare una nuova operazione
    public PrimaNota createPrimaNota(PrimaNota primaNota) {
        return primaNotaRepository.save(primaNota);
    }

    // Modificare un'operazione esistente
    public PrimaNota updatePrimaNota(Long id, PrimaNota updatedPrimaNota) {
        return primaNotaRepository.findById(id).map(existing -> {
            existing.setDataOperazione(updatedPrimaNota.getDataOperazione());
            existing.setNome(updatedPrimaNota.getNome());
            existing.setCategoria(updatedPrimaNota.getCategoria());
            existing.setMetodoPagamento(updatedPrimaNota.getMetodoPagamento());
            existing.setImporto(updatedPrimaNota.getImporto());
            existing.setTipoMovimento(updatedPrimaNota.getTipoMovimento());
            existing.setFattura(updatedPrimaNota.getFattura());
            existing.setIncaricoId(updatedPrimaNota.getIncaricoId());
            return primaNotaRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Operazione non trovata"));
    }

    // Eliminare un'operazione
    public void deletePrimaNota(Long id) {
        primaNotaRepository.deleteById(id);
    }

        public Map<String, BigDecimal> getTotaleEntrateUscite(Long userId, LocalDate startDate, LocalDate endDate) {
        BigDecimal totaleEntrate = primaNotaRepository
            .findByUserIdAndTipoMovimentoAndDataOperazioneBetween(userId, TipoMovimento.ENTRATA, startDate, endDate)
            .stream().map(PrimaNota::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totaleUscite = primaNotaRepository
            .findByUserIdAndTipoMovimentoAndDataOperazioneBetween(userId, TipoMovimento.USCITA, startDate, endDate)
            .stream().map(PrimaNota::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> result = new HashMap<>();
        result.put("entrate", totaleEntrate);
        result.put("uscite", totaleUscite);
        return result;
    }

    // ✅ Storico del saldo mensile
    public List<Map<String, Object>> getSaldoMensile(Long userId, int mesi) {
        List<Map<String, Object>> saldoMensile = new ArrayList<>();
        LocalDate oggi = LocalDate.now();

        for (int i = 0; i < mesi; i++) {
            LocalDate start = oggi.minusMonths(i).withDayOfMonth(1);
            LocalDate end = start.plusMonths(1).minusDays(1);

            BigDecimal entrate = primaNotaRepository
                .findByUserIdAndTipoMovimentoAndDataOperazioneBetween(userId, TipoMovimento.ENTRATA, start, end)
                .stream().map(PrimaNota::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal uscite = primaNotaRepository
                .findByUserIdAndTipoMovimentoAndDataOperazioneBetween(userId, TipoMovimento.USCITA, start, end)
                .stream().map(PrimaNota::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal saldo = entrate.subtract(uscite);

            Map<String, Object> meseData = new HashMap<>();
            meseData.put("mese", start.getMonth().toString());
            meseData.put("entrate", entrate);
            meseData.put("uscite", uscite);
            meseData.put("saldo", saldo);

            saldoMensile.add(meseData);
        }

        return saldoMensile;
    }

    // ✅ Distribuzione delle categorie (Quante entrate/uscite per categoria)
    public Map<String, BigDecimal> getDistribuzioneCategorie(Long userId, TipoMovimento tipoMovimento) {
        List<PrimaNota> transazioni = primaNotaRepository.findByUserIdAndTipoMovimento(userId, tipoMovimento);
        
        Map<String, BigDecimal> distribuzione = new HashMap<>();
        for (PrimaNota transazione : transazioni) {
            String categoria = transazione.getCategoria().getNome();
            distribuzione.put(categoria, distribuzione.getOrDefault(categoria, BigDecimal.ZERO).add(transazione.getImporto()));
        }
        return distribuzione;
    }
}
