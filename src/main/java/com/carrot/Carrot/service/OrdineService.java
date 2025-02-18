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

        if (ordine.getDettagliOrdine() != null) {
            for (DettaglioOrdine dettaglio : ordine.getDettagliOrdine()) {
                dettaglio.setOrdine(ordine);
                
                // **Scalare la quantità se il prodotto è esauribile**
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
        int annoCorrente = LocalDate.now().getYear();
        int progressivo = ordineRepository.countByUserAndYear(ordine.getUser().getId(), annoCorrente) + 1;
        String numeroOrdine = annoCorrente + "-" + String.format("%03d", progressivo);
        ordine.setNumero_ordine(numeroOrdine);
        ordineRepository.save(ordine);

        if (ordine.getCliente() != null) {
            ordine.getCliente().setDataUltimoOrdine(LocalDate.now());
            clienteRepository.save(ordine.getCliente());
        }

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

        if (existingOrdineOpt.isPresent()) {
            Ordine existingOrdine = existingOrdineOpt.get();

            // **Ripristina le quantità dei prodotti esauribili prima di aggiornarli**
            for (DettaglioOrdine oldDettaglio : existingOrdine.getDettagliOrdine()) {
                Prodotto prodotto = oldDettaglio.getProdotto();
                if (prodotto.isEsauribile()) {
                    prodotto.setQuantita(prodotto.getQuantita() + oldDettaglio.getQuantita());
                    prodottoRepository.save(prodotto);
                }
            }

            // **Aggiorna l'ordine con i nuovi dettagli**
            existingOrdine.getDettagliOrdine().clear();
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

            BigDecimal totale = existingOrdine.getDettagliOrdine()
                .stream()
                .map(d -> d.getPrezzoUnitario().multiply(BigDecimal.valueOf(d.getQuantita())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            existingOrdine.setTotale(totale);

            Ordine ordineAggiornato = ordineRepository.save(existingOrdine);

            Operazione operazione = new Operazione(
                    "Ordine", "Modifica",
                    "Ordine modificato: " + ordine.getId(),
                    LocalDateTime.now(),
                    currentUser);
            operazione.setUser(currentUser);
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
