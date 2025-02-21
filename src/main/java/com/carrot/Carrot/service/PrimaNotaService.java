package com.carrot.Carrot.service;

import com.carrot.Carrot.enumerator.TipoMovimento;
import com.carrot.Carrot.model.DettaglioOrdine;
import com.carrot.Carrot.model.Ordine;
import com.carrot.Carrot.model.PrimaNota;
import com.carrot.Carrot.model.User;
import com.carrot.Carrot.repository.DettaglioOrdineRepository;
import com.carrot.Carrot.repository.OrdineRepository;
import com.carrot.Carrot.repository.PrimaNotaRepository;
import com.carrot.Carrot.security.MyUserDetails;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PrimaNotaService {

    @Autowired
    private PrimaNotaRepository primaNotaRepository;
    @Autowired
    private DettaglioOrdineRepository dettaglioOrdineRepository;
    @Autowired
    private OrdineRepository ordineRepository;

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
        
        // Se il tipo di movimento è USCITA e l'importo è positivo, lo converto in negativo
        if (primaNota.getTipoMovimento() == TipoMovimento.USCITA &&
            primaNota.getImporto() != null &&
            primaNota.getImporto().compareTo(BigDecimal.ZERO) > 0) {
                primaNota.setImporto(primaNota.getImporto().negate());
        }
        
        // Salvataggio della prima nota
        return primaNotaRepository.save(primaNota);
    }
    
    // Modificare un'operazione esistente
    public PrimaNota updatePrimaNota(Long id, PrimaNota updatedPrimaNota) {
        return primaNotaRepository.findByIdAndUserId(id, getCurrentUser().getId())
                .map(existing -> {
                    existing.setDataOperazione(updatedPrimaNota.getDataOperazione());
                    existing.setNome(updatedPrimaNota.getNome());
                    existing.setCategoria(updatedPrimaNota.getCategoria());
                    existing.setMetodoPagamento(updatedPrimaNota.getMetodoPagamento());
                    existing.setImporto(updatedPrimaNota.getImporto());
                    existing.setTipoMovimento(updatedPrimaNota.getTipoMovimento());
                    existing.setFattura(updatedPrimaNota.getFattura());
                    existing.setIncaricoId(updatedPrimaNota.getIncaricoId());
                    return primaNotaRepository.save(existing);
                })
                // Se non esiste l'operazione con quell'ID e userId, scegliamo se restituire null 
                // o un'eccezione custom (rimossa o gestita diversamente, se non vogliamo alzare eccezioni).
                .orElse(null);
    }

    // Eliminare un'operazione
    @Transactional
    public void deletePrimaNota(Long id) {
        // Non lancia eccezioni se l'ID non esiste; 
        // semplicemente non elimina nulla (metodo custom del repository).
        primaNotaRepository.deleteByIdAndUserId(id, getCurrentUser().getId());
    }

    // Ottenere il totale di entrate e uscite
    public Map<String, BigDecimal> getTotaleEntrateUscite(LocalDate dataInizio, LocalDate dataFine) {
        List<PrimaNota> operazioni = primaNotaRepository.findByUserId(getCurrentUser().getId())
                .stream()
                .filter(op -> !op.getDataOperazione().isBefore(dataInizio) && !op.getDataOperazione().isAfter(dataFine))
                .collect(Collectors.toList());
    
        BigDecimal entrate = operazioni.stream()
                .filter(p -> p.getTipoMovimento() == TipoMovimento.ENTRATA)
                .map(PrimaNota::getImporto)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    
        BigDecimal uscite = operazioni.stream()
                .filter(p -> p.getTipoMovimento() == TipoMovimento.USCITA)
                .map(PrimaNota::getImporto)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    
        Map<String, BigDecimal> result = new HashMap<>();
        result.put("entrate", entrate);
        result.put("uscite", uscite);
        return result;
    }
    
    // Metodo helper per un periodo "rolling" negli ultimi 'giorni' giorni
    public Map<String, BigDecimal> getTotaleEntrateUsciteRolling(int giorni) {
        LocalDate dataFine = LocalDate.now();
        LocalDate dataInizio = dataFine.minusDays(giorni);
        return getTotaleEntrateUscite(dataInizio, dataFine);
    }

    public List<Map<String, Object>> getSaldoMensile(int mesi) {
        List<Map<String, Object>> saldoMensile = new ArrayList<>();
        LocalDate oggi = LocalDate.now();

        for (int i = 0; i < mesi; i++) {
            LocalDate start = oggi.minusMonths(i).withDayOfMonth(1);
            LocalDate end = start.plusMonths(1).minusDays(1);

            BigDecimal entrate = primaNotaRepository
                    .findByUserIdAndTipoMovimentoAndDataOperazioneBetween(getCurrentUser().getId(), TipoMovimento.ENTRATA, start, end)
                    .stream()
                    .map(PrimaNota::getImporto)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal uscite = primaNotaRepository
                    .findByUserIdAndTipoMovimentoAndDataOperazioneBetween(getCurrentUser().getId(), TipoMovimento.USCITA, start, end)
                    .stream()
                    .map(PrimaNota::getImporto)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> meseData = new HashMap<>();
            meseData.put("mese", start.getMonth().toString());
            meseData.put("entrate", entrate);
            meseData.put("uscite", uscite);
            meseData.put("saldo", entrate.add(uscite));

            saldoMensile.add(meseData);
        }
        return saldoMensile;
    }

    // Ottenere la distribuzione delle categorie
    public Map<String, BigDecimal> getDistribuzioneCategorie(TipoMovimento tipoMovimento) {
        List<PrimaNota> operazioni = primaNotaRepository.findByUserIdAndTipoMovimento(getCurrentUser().getId(), tipoMovimento);

        // Se la categoria è null, restituiamo "Senza categoria"
        return operazioni.stream()
                .collect(Collectors.groupingBy(
                        p -> (p.getCategoria() == null || p.getCategoria().getNome() == null)
                                ? "Senza categoria"
                                : p.getCategoria().getNome(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                // Se l'importo è null, consideriamolo 0
                                p -> p.getImporto() == null ? BigDecimal.ZERO : p.getImporto(),
                                BigDecimal::add
                        )
                ));
    }

    public Ordine getIncarico(Long incaricoId) {
        if (incaricoId == null) {
            return null;
        }
        return ordineRepository.findById(incaricoId).orElse(null);
    }

    // Mappa i prodotti più costosi delle uscite
    public Map<String, BigDecimal> getProdottiPiuCostosiInUscite() {
        Long userId = getCurrentUser().getId();

        // Recupera tutte le uscite dell'utente autenticato
        List<PrimaNota> uscite = primaNotaRepository.findByUserIdAndTipoMovimento(userId, TipoMovimento.USCITA);

        // Mappa per sommare le uscite per ciascun incarico (ordine)
        Map<Long, BigDecimal> uscitePerIncarico = new HashMap<>();

        for (PrimaNota uscita : uscite) {
            if (uscita.getIncaricoId() != null) {
                BigDecimal importo = uscita.getImporto() == null ? BigDecimal.ZERO : uscita.getImporto();
                uscitePerIncarico.merge(uscita.getIncaricoId(), importo, BigDecimal::add);
            }
        }

        // Recupera i dettagli degli ordini associati a quegli incarichi
        if (uscitePerIncarico.isEmpty()) {
            // Se non abbiamo nessun incarico, restituiamo mappa vuota
            return Collections.emptyMap();
        }

        List<DettaglioOrdine> dettagliOrdini = dettaglioOrdineRepository.findByOrdineIdIn(uscitePerIncarico.keySet());

        // Mappa per raccogliere il totale delle uscite generate da ciascun prodotto
        Map<String, BigDecimal> prodottiConPiuUscite = new HashMap<>();

        for (DettaglioOrdine dettaglio : dettagliOrdini) {
            // Controlli di null su Prodotto e Ordine
            if (dettaglio.getProdotto() == null || dettaglio.getOrdine() == null) {
                continue;
            }
            String nomeProdotto = dettaglio.getProdotto().getNome() != null
                    ? dettaglio.getProdotto().getNome()
                    : "Prodotto non specificato";

            Long idOrdine = dettaglio.getOrdine().getId();
            if (idOrdine == null) {
                continue;
            }

            BigDecimal totaleUscitePerIncarico =
                    uscitePerIncarico.getOrDefault(idOrdine, BigDecimal.ZERO);

            // Somma il totale delle uscite relative a quell'incarico ai prodotti coinvolti
            prodottiConPiuUscite.merge(nomeProdotto, totaleUscitePerIncarico, BigDecimal::add);
        }

        return prodottiConPiuUscite;
    }

    // Mappa prodotti con il rapporto entrate/uscite
    public Map<String, BigDecimal> getProdottiConRapportoEntrateUscite() {
        Long userId = getCurrentUser().getId();

        // Recupera tutte le entrate e uscite dell'utente
        List<PrimaNota> entrate = primaNotaRepository.findByUserIdAndTipoMovimento(userId, TipoMovimento.ENTRATA);
        List<PrimaNota> uscite = primaNotaRepository.findByUserIdAndTipoMovimento(userId, TipoMovimento.USCITA);

        // Mappa per sommare entrate e uscite per incarico (ordine)
        Map<Long, BigDecimal> totaleEntratePerIncarico = new HashMap<>();
        Map<Long, BigDecimal> totaleUscitePerIncarico = new HashMap<>();

        for (PrimaNota e : entrate) {
            if (e.getIncaricoId() != null) {
                BigDecimal importoEntr = e.getImporto() == null ? BigDecimal.ZERO : e.getImporto();
                totaleEntratePerIncarico.merge(e.getIncaricoId(), importoEntr, BigDecimal::add);
            }
        }

        for (PrimaNota u : uscite) {
            if (u.getIncaricoId() != null) {
                BigDecimal importoUsc = u.getImporto() == null ? BigDecimal.ZERO : u.getImporto();
                totaleUscitePerIncarico.merge(u.getIncaricoId(), importoUsc, BigDecimal::add);
            }
        }

        // Intersechiamo solo gli incarichi che effettivamente esistono come chiavi
        // (anche se potremmo leggerli separatamente)
        Set<Long> incarichi = new HashSet<>();
        incarichi.addAll(totaleEntratePerIncarico.keySet());
        incarichi.addAll(totaleUscitePerIncarico.keySet());

        if (incarichi.isEmpty()) {
            return Collections.emptyMap();
        }

        // Recupera i dettagli degli ordini associati
        List<DettaglioOrdine> dettagliOrdini = dettaglioOrdineRepository.findByOrdineIdIn(incarichi);

        // Mappa per raccogliere il rapporto Entrate/Uscite per ogni prodotto
        Map<String, BigDecimal> rapportoProdotti = new HashMap<>();

        for (DettaglioOrdine dettaglio : dettagliOrdini) {
            if (dettaglio.getOrdine() == null || dettaglio.getProdotto() == null) {
                continue;
            }
            Long incaricoId = dettaglio.getOrdine().getId();
            String nomeProdotto = 
                    dettaglio.getProdotto().getNome() != null 
                    ? dettaglio.getProdotto().getNome()
                    : "Prodotto non specificato";

            BigDecimal totaleEntrate = 
                    totaleEntratePerIncarico.getOrDefault(incaricoId, BigDecimal.ZERO);
            BigDecimal totaleUscite = 
                    totaleUscitePerIncarico.getOrDefault(incaricoId, BigDecimal.ZERO);

            // Calcolo solo se totaleUscite > 0
            if (totaleUscite.compareTo(BigDecimal.ZERO) > 0) {
                // Evitiamo eccezioni di divisione
                BigDecimal rapporto = totaleEntrate
                        .divide(totaleUscite, 2, RoundingMode.HALF_UP);

                rapportoProdotti.merge(nomeProdotto, rapporto, BigDecimal::add);
            }
        }

        // Ordiniamo in ordine decrescente
        return rapportoProdotti.entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
}
