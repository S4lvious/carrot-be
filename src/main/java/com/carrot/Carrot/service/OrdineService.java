package com.carrot.Carrot.service;

import com.carrot.Carrot.model.Ordine;
import com.carrot.Carrot.model.Prodotto;
import com.carrot.Carrot.model.DettaglioOrdine;
import com.carrot.Carrot.model.Operazione;
import com.carrot.Carrot.model.Cliente;
import com.carrot.Carrot.model.User;
import com.carrot.Carrot.repository.OrdineRepository;
import com.carrot.Carrot.repository.ProdottoRepository;
import com.carrot.Carrot.repository.ClienteRepository;
import com.carrot.Carrot.repository.DettaglioOrdineRepository;
import com.carrot.Carrot.repository.OperazioneRepository;
import com.carrot.Carrot.security.MyUserDetails;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrdineService {

    private final OrdineRepository ordineRepository;
    private final ClienteRepository clienteRepository;
    private final DettaglioOrdineRepository dettaglioOrdineRepository;
    private final OperazioneRepository operazioneRepository;
    private final ProdottoRepository prodottoRepository;

    public OrdineService(OrdineRepository ordineRepository, ClienteRepository clienteRepository,
                         DettaglioOrdineRepository dettaglioOrdineRepository, OperazioneRepository operazioneRepository, ProdottoRepository prodottoRepository) {
        this.ordineRepository = ordineRepository;
        this.clienteRepository = clienteRepository;
        this.dettaglioOrdineRepository = dettaglioOrdineRepository;
        this.operazioneRepository = operazioneRepository;
        this.prodottoRepository = prodottoRepository;
    }

    // Metodo di supporto per ottenere l'utente autenticato
    private User getCurrentUser() {
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder
                                        .getContext()
                                        .getAuthentication()
                                        .getPrincipal();
        return userDetails.getUser();
    }

    // Restituisce tutti gli ordini dell'utente corrente
    public List<Ordine> getAllOrdini() {
        Long currentUserId = getCurrentUser().getId();
        return ordineRepository.findByUserId(currentUserId);
    }

    // Restituisce gli ordini non fatturati dell'utente corrente
    public List<Ordine> getAllOrdiniNotInvoicedList() {
        Long currentUserId = getCurrentUser().getId();
        return ordineRepository.findByFatturatoAndUserId(false, currentUserId);
    }

    // Restituisce l'ordine cercato solo se appartiene all'utente corrente
    public Optional<Ordine> getOrdineById(Long id) {
        Long currentUserId = getCurrentUser().getId();
        return ordineRepository.findByIdAndUserId(id, currentUserId);
    }

    @Transactional
    public Optional<Ordine> addOrdine(Ordine ordine) {
        User currentUser = getCurrentUser();
        ordine.setUser(currentUser);
    
        // Se l'ordine contiene già dei DettagliOrdine, effettuiamo i controlli sulle quantità
        if (ordine.getDettagliOrdine() != null) {
            for (DettaglioOrdine dettaglio : ordine.getDettagliOrdine()) {
                dettaglio.setOrdine(ordine);
    
                // **Scalare la quantità se il prodotto è esauribile**
                Prodotto prodotto = dettaglio.getProdotto();
                if (prodotto.isEsauribile()) {
                    int nuovaQuantita = prodotto.getQuantita() - dettaglio.getQuantita();
                    if (nuovaQuantita < 0) {
                        throw new IllegalArgumentException(
                            "Quantità insufficiente per il prodotto: " + prodotto.getNome());
                    }
                    prodotto.setQuantita(nuovaQuantita);
                    prodottoRepository.save(prodotto);
                }
            }
        }
    
        // Numero Ordine progressivo per l'anno corrente
        int annoCorrente = LocalDate.now().getYear();
        int progressivo = ordineRepository.countByUserAndYear(ordine.getUser().getId(), annoCorrente) + 1;
        String numeroOrdine = annoCorrente + "-" + String.format("%03d", progressivo);
        ordine.setNumeroOrdine(numeroOrdine);
    
        // Calcolo del totale (solo dalla somma dei dettagli)
        // Se vuoi includere bollo o altre voci, aggiungi la logica desiderata
        BigDecimal totale = ordine.getDettagliOrdine() == null
            ? BigDecimal.ZERO
            : ordine.getDettagliOrdine().stream()
                .map(d -> d.getPrezzoUnitario().multiply(BigDecimal.valueOf(d.getQuantita())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        ordine.setTotale(totale);
    
        // Imposta eventuali nuovi campi dell'Ordine (se arrivano dal front-end/DTO)
        // Se i campi sono già popolati in "ordine", non devi fare altro
        // Esempio:
        // ordine.setTipoDocumento(ordine.getTipoDocumento());
        // ordine.setCausale(ordine.getCausale());
        // ordine.setBolloVirtuale(ordine.getBolloVirtuale());
        // ordine.setImportoBollo(ordine.getImportoBollo());
        // ... e così via per i campi di cassa, pagamento, documenti di riferimento, ecc.
        // (Se i valori sono già nel `ordine` in ingresso, non c'è nulla da sovrascrivere.)
    
        ordineRepository.save(ordine);
    
        // Aggiorna data ultimo ordine per il cliente
        if (ordine.getCliente() != null) {
            ordine.getCliente().setDataUltimoOrdine(LocalDate.now());
            clienteRepository.save(ordine.getCliente());
        }
    
        // Tracciamento operazione
        Operazione operazione = new Operazione(
                "Ordine", "Aggiunta",
                "Ordine creato: " + ordine.getId(),
                LocalDateTime.now(),
                currentUser);
        operazione.setUser(currentUser);
        operazioneRepository.save(operazione);
    
        return Optional.of(ordine);
    }
    
    @Transactional
public Optional<Ordine> updateOrdine(Ordine ordine) {
    Long currentUserId = getCurrentUser().getId();
    User currentUser = getCurrentUser();

    Optional<Ordine> existingOrdineOpt = ordineRepository.findByIdAndUserId(ordine.getId(), currentUserId);
    if (existingOrdineOpt.isEmpty()) {
        return Optional.empty();
    }

    Ordine existingOrdine = existingOrdineOpt.get();

    // 1) Ripristina le quantità dei prodotti esauribili
    for (DettaglioOrdine oldDettaglio : existingOrdine.getDettagliOrdine()) {
        Prodotto prodotto = oldDettaglio.getProdotto();
        if (prodotto.isEsauribile()) {
            // Riaggiungiamo la quantità prima di sostituire i dettagli
            prodotto.setQuantita(prodotto.getQuantita() + oldDettaglio.getQuantita());
            prodottoRepository.save(prodotto);
        }
    }

    // 2) Svuota i vecchi dettagli
    existingOrdine.getDettagliOrdine().clear();

    // 3) Aggiungi i nuovi dettagli
    if (ordine.getDettagliOrdine() != null) {
        for (DettaglioOrdine dettaglio : ordine.getDettagliOrdine()) {
            dettaglio.setOrdine(existingOrdine);
            existingOrdine.getDettagliOrdine().add(dettaglio);

            Prodotto prodotto = dettaglio.getProdotto();
            if (prodotto.isEsauribile()) {
                int nuovaQuantita = prodotto.getQuantita() - dettaglio.getQuantita();
                if (nuovaQuantita < 0) {
                    throw new IllegalArgumentException("Quantità insufficiente per il prodotto: " + prodotto.getNome());
                }
                prodotto.setQuantita(nuovaQuantita);
                prodottoRepository.save(prodotto);
            }
        }
    }

    // 4) Ricalcola il totale dell'ordine
    BigDecimal totale = existingOrdine.getDettagliOrdine().stream()
        .map(d -> d.getPrezzoUnitario().multiply(BigDecimal.valueOf(d.getQuantita())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    existingOrdine.setTotale(totale);

    // 5) Se ci sono altri campi base dell'Ordine da aggiornare (es. stato), impostali qui
    existingOrdine.setStato(ordine.getStato());
    // existingOrdine.setNumeroOrdine(ordine.getNumeroOrdine()); // se vuoi permettere di cambiare
    // ... Qualsiasi altro campo effettivamente rimasto in Ordine

    // 6) Salva modifiche
    Ordine ordineAggiornato = ordineRepository.save(existingOrdine);

    // 7) Tracciamento operazione
    Operazione operazione = new Operazione(
        "Ordine", "Modifica",
        "Ordine modificato: " + ordine.getId(),
        LocalDateTime.now(),
        currentUser
    );
    operazione.setUser(currentUser);
    operazioneRepository.save(operazione);

    return Optional.of(ordineAggiornato);
}


    @Transactional
    public void deleteOrdine(Long id) {
        Long currentUserId = getCurrentUser().getId();
        User currentUser = getCurrentUser();
        Optional<Ordine> ordineOpt = ordineRepository.findByIdAndUserId(id, currentUserId);
        if (ordineOpt.isPresent()) {
            Ordine ordine = ordineOpt.get();
            // Elimina prima i dettagli se non hai il cascade impostato per la cancellazione
            dettaglioOrdineRepository.deleteAll(ordine.getDettagliOrdine());
            ordineRepository.delete(ordine);
            
            Operazione operazione = new Operazione(
                    "Ordine", "Eliminazione",
                    "Ordine eliminato: " + ordine.getId(),
                    LocalDateTime.now(),
                    currentUser
                    );
            operazione.setUser(getCurrentUser());
            operazioneRepository.save(operazione);
        }
    }
}
