package com.carrot.Carrot.service;

import com.carrot.Carrot.model.Ordine;
import com.carrot.Carrot.model.Prodotto;
import com.carrot.Carrot.model.DettaglioOrdine;
import com.carrot.Carrot.model.Documento;
import com.carrot.Carrot.model.Operazione;
import com.carrot.Carrot.model.User;
import com.carrot.Carrot.repository.OrdineRepository;
import com.carrot.Carrot.repository.ProdottoRepository;
import com.carrot.Carrot.repository.ClienteRepository;
import com.carrot.Carrot.repository.DettaglioOrdineRepository;
import com.carrot.Carrot.repository.DocumentoRepository;
import com.carrot.Carrot.repository.OperazioneRepository;
import com.carrot.Carrot.security.MyUserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;

import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrdineService {

    private final OrdineRepository ordineRepository;
    private final ClienteRepository clienteRepository;
    private final DettaglioOrdineRepository dettaglioOrdineRepository;
    private final OperazioneRepository operazioneRepository;
    private final ProdottoRepository prodottoRepository;
    private final DocumentoRepository documentoRepository;
    private final StorageService storage;


    public OrdineService(OrdineRepository ordineRepository,
                         ClienteRepository clienteRepository,
                         DettaglioOrdineRepository dettaglioOrdineRepository, 
                         OperazioneRepository operazioneRepository, 
                         ProdottoRepository prodottoRepository, 
                         DocumentoRepository documentoRepository, StorageService storage
                         ) {
        this.ordineRepository = ordineRepository;
        this.clienteRepository = clienteRepository;
        this.dettaglioOrdineRepository = dettaglioOrdineRepository;
        this.operazioneRepository = operazioneRepository;
        this.prodottoRepository = prodottoRepository;
        this.documentoRepository = documentoRepository;
        this.storage = storage;
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
    public Optional<Ordine> addOrdine(Ordine ordine, List<MultipartFile> documenti) {
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
        BigDecimal totale = ordine.getDettagliOrdine() == null
            ? BigDecimal.ZERO
            : ordine.getDettagliOrdine().stream()
                .map(d -> d.getPrezzoUnitario().multiply(BigDecimal.valueOf(d.getQuantita())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        ordine.setTotale(totale);
    
        // ✅ 1. Salviamo l'ordine PRIMA di gestire i documenti (necessario per ottenere l'ID)
        ordineRepository.save(ordine);
    
        // ✅ 2. Carichiamo i documenti solo se presenti
        if (documenti != null && !documenti.isEmpty()) {
            List<Documento> documentiSalvati = documenti.stream()
                    .map(file -> uploadDocumento(file, ordine))
                    .collect(Collectors.toList());
            documentoRepository.saveAll(documentiSalvati);
        }
    
        // ✅ 3. Aggiorna data ultimo ordine per il cliente
        if (ordine.getCliente() != null) {
            ordine.getCliente().setDataUltimoOrdine(LocalDate.now());
            clienteRepository.save(ordine.getCliente());
        }
    
        // ✅ 4. Tracciamento operazione
        Operazione operazione = new Operazione(
                "Ordine", "Aggiunta",
                "Ordine creato: " + ordine.getId(),
                LocalDateTime.now(),
                currentUser);
        operazione.setUser(currentUser);
        operazioneRepository.save(operazione);
    
        return Optional.of(ordine);
    }

    private Documento uploadDocumento(MultipartFile file, Ordine ordine) {
        try {
            // ✅ Chiamiamo il metodo di StorageService per l'upload
            String filePath = storage.uploadFile(file, ordine.getId().toString());

            Documento documento = new Documento();
            documento.setNome(file.getOriginalFilename());
            documento.setPercorso(filePath); // Salviamo solo il percorso
            documento.setOrdine(ordine);

            return documento;
        } catch (IOException e) {
            throw new RuntimeException("Errore nell'upload del file: " + file.getOriginalFilename(), e);
        }
    }
        
    @Transactional
public Optional<Ordine> updateOrdine(Long id, Ordine ordine, List<MultipartFile> documenti) {
    Long currentUserId = getCurrentUser().getId();
    User currentUser = getCurrentUser();

    Optional<Ordine> existingOrdineOpt = ordineRepository.findByIdAndUserId(id, currentUserId);
    if (existingOrdineOpt.isEmpty()) {
        return Optional.empty();
    }

    Ordine existingOrdine = existingOrdineOpt.get();

    // 1) Ripristina le quantità dei prodotti esauribili
    for (DettaglioOrdine oldDettaglio : existingOrdine.getDettagliOrdine()) {
        Prodotto prodotto = oldDettaglio.getProdotto();
        if (prodotto.isEsauribile()) {
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

    // 5) Aggiorna i campi base dell'Ordine
    existingOrdine.setStato(ordine.getStato());

    // ✅ 6) Carica e aggiungi i nuovi documenti (senza eliminare quelli esistenti)
    if (documenti != null && !documenti.isEmpty()) {
        List<Documento> nuoviDocumenti = documenti.stream()
                .map(file -> uploadDocumento(file, existingOrdine))
                .collect(Collectors.toList());
        documentoRepository.saveAll(nuoviDocumenti);
    }

    // 7) Salva modifiche all'ordine
    Ordine ordineAggiornato = ordineRepository.save(existingOrdine);

    // 8) Tracciamento operazione
    Operazione operazione = new Operazione(
            "Ordine", "Modifica",
            "Ordine modificato: " + ordine.getId(),
            LocalDateTime.now(),
            currentUser);
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
