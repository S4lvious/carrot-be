package com.carrot.Carrot.service;

import com.carrot.Carrot.model.Ordine;
import com.carrot.Carrot.model.DettaglioOrdine;
import com.carrot.Carrot.model.Operazione;
import com.carrot.Carrot.model.Cliente;
import com.carrot.Carrot.model.User;
import com.carrot.Carrot.repository.OrdineRepository;
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

    public OrdineService(OrdineRepository ordineRepository, ClienteRepository clienteRepository,
                         DettaglioOrdineRepository dettaglioOrdineRepository, OperazioneRepository operazioneRepository) {
        this.ordineRepository = ordineRepository;
        this.clienteRepository = clienteRepository;
        this.dettaglioOrdineRepository = dettaglioOrdineRepository;
        this.operazioneRepository = operazioneRepository;
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
        // Associa l'ordine all'utente corrente
        User currentUser = getCurrentUser();
        ordine.setUser(currentUser);

        // Imposta il riferimento dell'ordine in ogni dettaglio
        if (ordine.getDettagliOrdine() != null) {
            for (DettaglioOrdine dettaglio : ordine.getDettagliOrdine()) {
                dettaglio.setOrdine(ordine);
            }
        }
        
        // Salva l'ordine (cascade ALL si occuperà di salvare anche i dettagli)
        ordineRepository.save(ordine);
        
        // Aggiornamento del cliente: (opzionale, ma si può controllare anche che il cliente appartenga all'utente corrente)
        Cliente cliente = ordine.getCliente();
        if (cliente != null) {
            cliente.setDataUltimoOrdine(LocalDate.now());
            clienteRepository.save(cliente);
        }
        
        Operazione operazione = new Operazione(
                "Ordine", "Aggiunta",
                "Ordine creato: " + ordine.getId() + " per cliente " + (cliente != null ? cliente.getId() : "N/A"),
                LocalDateTime.now(),
                currentUser);
        // Associa l'operazione all'utente corrente
        operazione.setUser(currentUser);
        operazioneRepository.save(operazione);
        
        return Optional.of(ordine);
    }
    
    @Transactional
    public Optional<Ordine> updateOrdine(Ordine ordine) {
        Long currentUserId = getCurrentUser().getId();
        User currentUser = getCurrentUser();
        Optional<Ordine> existingOrdineOpt = ordineRepository.findByIdAndUserId(ordine.getId(), currentUserId);
        
        if (existingOrdineOpt.isPresent()) {
            Ordine existingOrdine = existingOrdineOpt.get();
    
            // Aggiorna i campi principali
            existingOrdine.setCliente(ordine.getCliente());
            existingOrdine.setStato(ordine.getStato());
            existingOrdine.setFatturato(ordine.isFatturato());
    
            // Gestione dettagli ordine:
            // Pulisci la lista dei dettagli esistenti
            existingOrdine.getDettagliOrdine().clear();
    
            // Per ogni dettaglio nel nuovo ordine, imposta il riferimento all'ordine e aggiungilo
            for (DettaglioOrdine dettaglio : ordine.getDettagliOrdine()) {
                dettaglio.setOrdine(existingOrdine);
                existingOrdine.getDettagliOrdine().add(dettaglio);
            }
    
            // Aggiorna il totale sommando (prezzoUnitario * quantita) per ciascun dettaglio
            BigDecimal totale = existingOrdine.getDettagliOrdine()
                .stream()
                .map(d -> d.getPrezzoUnitario().multiply(BigDecimal.valueOf(d.getQuantita())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            existingOrdine.setTotale(totale);
    
            // Salva l'ordine aggiornato (grazie al cascade, i nuovi dettagli saranno salvati e gli orfani eliminati)
            Ordine ordineAggiornato = ordineRepository.save(existingOrdine);
    
            // Registra l'operazione
            Operazione operazione = new Operazione(
                    "Ordine", "Modifica",
                    "Ordine modificato: " + ordine.getId(),
                    LocalDateTime.now(),
                    currentUser);
            operazione.setUser(getCurrentUser());
            operazioneRepository.save(operazione);
    
            return Optional.of(ordineAggiornato);
        }
        return Optional.empty();
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
