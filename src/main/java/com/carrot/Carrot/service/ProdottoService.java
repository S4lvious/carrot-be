package com.carrot.Carrot.service;

import com.carrot.Carrot.model.Prodotto;
import com.carrot.Carrot.model.Operazione;
import com.carrot.Carrot.model.User;
import com.carrot.Carrot.repository.ProdottoRepository;
import com.carrot.Carrot.repository.OperazioneRepository;
import com.carrot.Carrot.security.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProdottoService {

    private final ProdottoRepository prodottoRepository;
    private final OperazioneRepository operazioneRepository;

    @Autowired
    public ProdottoService(ProdottoRepository prodottoRepository, OperazioneRepository operazioneRepository) {
        this.prodottoRepository = prodottoRepository;
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

    // Restituisce tutti i prodotti dell'utente corrente
    public List<Prodotto> getAllProdotti() {
        Long currentUserId = getCurrentUser().getId();
        return prodottoRepository.findByUserId(currentUserId);
    }

    // Restituisce il prodotto se appartiene all'utente corrente
    public Optional<Prodotto> getProdottoById(Long id) {
        Long currentUserId = getCurrentUser().getId();
        return prodottoRepository.findByIdAndUserId(id, currentUserId);
    }

    // Aggiunge un nuovo prodotto, associandolo all'utente corrente
    public Prodotto addProdotto(Prodotto prodotto) {
        User currentUser = getCurrentUser();
        prodotto.setUser(currentUser);
        Prodotto nuovoProdotto = prodottoRepository.save(prodotto);

        // Registra l'operazione, associandola all'utente corrente
        Operazione operazione = new Operazione(
                "Prodotto",
                "Aggiunta",
                "Nuovo prodotto #" + nuovoProdotto.getId() + " creato",
                LocalDateTime.now(),
                getCurrentUser());
        operazioneRepository.save(operazione);

        return nuovoProdotto;
    }

    // Aggiorna il prodotto solo se appartiene all'utente corrente
    public Prodotto updateProdotto(Prodotto prodottoAggiornato) {
        Long currentUserId = getCurrentUser().getId();
        return prodottoRepository.findByIdAndUserId(prodottoAggiornato.getId(), currentUserId)
                .map(prodotto -> {
                    prodotto.setNome(prodottoAggiornato.getNome());
                    prodotto.setDescrizione(prodottoAggiornato.getDescrizione());
                    prodotto.setPrezzo(prodottoAggiornato.getPrezzo());
                    prodotto.setCategoria(prodottoAggiornato.getCategoria());
                    prodotto.setAliquotaIVA(prodottoAggiornato.getAliquotaIVA());
                    prodotto.setQuantita(prodottoAggiornato.getQuantita());
                    prodotto.setEsauribile(prodottoAggiornato.isEsauribile());

                    Prodotto prodottoModificato = prodottoRepository.save(prodotto);

                    // Registra l'operazione
                    Operazione operazione = new Operazione(
                            "Prodotto",
                            "Modifica",
                            "Modifica prodotto #" + prodotto.getId(),
                            LocalDateTime.now(),
                            getCurrentUser());
                    operazioneRepository.save(operazione);

                    return prodottoModificato;
                })
                .orElseThrow(() -> new RuntimeException("Prodotto non trovato con ID: " + prodottoAggiornato.getId()));
    }

    // Elimina il prodotto solo se appartiene all'utente corrente
    public void deleteProdotto(Long id) {
        Long currentUserId = getCurrentUser().getId();
        Optional<Prodotto> prodottoOpt = prodottoRepository.findByIdAndUserId(id, currentUserId);
        if (prodottoOpt.isPresent()) {
            prodottoRepository.delete(prodottoOpt.get());

            // Registra l'operazione
            Operazione operazione = new Operazione(
                    "Prodotto",
                    "Eliminazione",
                    "Eliminazione prodotto #" + id,
                    LocalDateTime.now(),
                    getCurrentUser());
            operazioneRepository.save(operazione);
        }
    }

    // Restituisce i prodotti con stock basso per l'utente corrente
    public List<Prodotto> getProdottiStockBasso(int soglia) {
        Long currentUserId = getCurrentUser().getId();
        return prodottoRepository.findByQuantitaLessThanEqualAndEsauribileIsTrueAndUserId(soglia, currentUserId);
    }
}
