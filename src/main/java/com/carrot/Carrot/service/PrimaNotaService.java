package com.carrot.Carrot.service;

import com.carrot.Carrot.enumerator.TipoMovimento;
import com.carrot.Carrot.model.DettaglioOrdine;
import com.carrot.Carrot.model.PrimaNota;
import com.carrot.Carrot.model.User;
import com.carrot.Carrot.repository.DettaglioOrdineRepository;
import com.carrot.Carrot.repository.PrimaNotaRepository;
import com.carrot.Carrot.security.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PrimaNotaService {

    @Autowired
    private PrimaNotaRepository primaNotaRepository;
    @Autowired
    private DettaglioOrdineRepository dettaglioOrdineRepository;


    // Metodo per ottenere l'utente autenticato
    private User getCurrentUser() {
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    // Ottenere tutte le operazioni dell'utente autenticato
    public List<PrimaNota> getAllByUser() {
        return primaNotaRepository.findByUserId(getCurrentUser().getId());
    }

    // Ottenere una singola operazione
    public Optional<PrimaNota> getById(Long id) {
        return primaNotaRepository.findByIdAndUserId(id, getCurrentUser().getId());
    }

    // Ottenere solo le ENTRATE o le USCITE
    public List<PrimaNota> getByTipo(TipoMovimento tipoMovimento) {
        return primaNotaRepository.findByUserIdAndTipoMovimento(getCurrentUser().getId(), tipoMovimento);
    }

    // Creare una nuova operazione
    public PrimaNota createPrimaNota(PrimaNota primaNota) {
        primaNota.setUser(getCurrentUser());
        return primaNotaRepository.save(primaNota);
    }

    // Modificare un'operazione esistente
    public PrimaNota updatePrimaNota(Long id, PrimaNota updatedPrimaNota) {
        return primaNotaRepository.findByIdAndUserId(id, getCurrentUser().getId()).map(existing -> {
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
        primaNotaRepository.deleteByIdAndUserId(id, getCurrentUser().getId());
    }

    // Ottenere il totale di entrate e uscite
    public Map<String, BigDecimal> getTotaleEntrateUscite() {
        List<PrimaNota> operazioni = primaNotaRepository.findByUserId(getCurrentUser().getId());
        BigDecimal entrate = operazioni.stream()
                .filter(p -> p.getTipoMovimento() == TipoMovimento.ENTRATA)
                .map(PrimaNota::getImporto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal uscite = operazioni.stream()
                .filter(p -> p.getTipoMovimento() == TipoMovimento.USCITA)
                .map(PrimaNota::getImporto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> result = new HashMap<>();
        result.put("entrate", entrate);
        result.put("uscite", uscite);
        return result;
    }

    // Ottenere il saldo mensile
    public List<Map<String, Object>> getSaldoMensile(int mesi) {
        List<Map<String, Object>> saldoMensile = new ArrayList<>();
        LocalDate oggi = LocalDate.now();

        for (int i = 0; i < mesi; i++) {
            LocalDate start = oggi.minusMonths(i).withDayOfMonth(1);
            LocalDate end = start.plusMonths(1).minusDays(1);

            BigDecimal entrate = primaNotaRepository.findByUserIdAndTipoMovimentoAndDataOperazioneBetween(getCurrentUser().getId(), TipoMovimento.ENTRATA, start, end)
                    .stream().map(PrimaNota::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal uscite = primaNotaRepository.findByUserIdAndTipoMovimentoAndDataOperazioneBetween(getCurrentUser().getId(), TipoMovimento.USCITA, start, end)
                    .stream().map(PrimaNota::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> meseData = new HashMap<>();
            meseData.put("mese", start.getMonth().toString());
            meseData.put("entrate", entrate);
            meseData.put("uscite", uscite);
            meseData.put("saldo", entrate.subtract(uscite));

            saldoMensile.add(meseData);
        }
        return saldoMensile;
    }

    // Ottenere la distribuzione delle categorie
    public Map<String, BigDecimal> getDistribuzioneCategorie(TipoMovimento tipoMovimento) {
        List<PrimaNota> operazioni = primaNotaRepository.findByUserIdAndTipoMovimento(getCurrentUser().getId(), tipoMovimento);
        return operazioni.stream()
                .collect(Collectors.groupingBy(p -> p.getCategoria().getNome(),
                        Collectors.reducing(BigDecimal.ZERO, PrimaNota::getImporto, BigDecimal::add)));
    }

        public Map<String, BigDecimal> getProdottiPiuCostosiInUscite() {
            Long userId = getCurrentUser().getId();

            // Recupera tutte le uscite dell'utente autenticato
            List<PrimaNota> uscite = primaNotaRepository.findByUserIdAndTipoMovimento(userId, TipoMovimento.USCITA);
            
            // Mappa per sommare le uscite per ciascun incarico (ordine)
            Map<Long, BigDecimal> uscitePerIncarico = new HashMap<>();
        
            for (PrimaNota uscita : uscite) {
                if (uscita.getIncaricoId() != null) {
                    uscitePerIncarico.put(
                        uscita.getIncaricoId(),
                        uscitePerIncarico.getOrDefault(uscita.getIncaricoId(), BigDecimal.ZERO).add(uscita.getImporto())
                    );
                }
            }
        
            // Recupera i dettagli degli ordini associati a quegli incarichi
            List<DettaglioOrdine> dettagliOrdini = dettaglioOrdineRepository.findByOrdineIdIn(uscitePerIncarico.keySet());
        
            // Mappa per raccogliere il totale delle uscite generate da ciascun prodotto
            Map<String, BigDecimal> prodottiConPiuUscite = new HashMap<>();
        
            for (DettaglioOrdine dettaglio : dettagliOrdini) {
                String nomeProdotto = dettaglio.getProdotto().getNome();
                BigDecimal totaleUscitePerIncarico = uscitePerIncarico.getOrDefault(dettaglio.getOrdine().getId(), BigDecimal.ZERO);
                
                // Somma il totale delle uscite relative a quell'incarico ai prodotti coinvolti
                prodottiConPiuUscite.put(
                    nomeProdotto,
                    prodottiConPiuUscite.getOrDefault(nomeProdotto, BigDecimal.ZERO).add(totaleUscitePerIncarico)
                );
            }
        
            return prodottiConPiuUscite;
            }    

}