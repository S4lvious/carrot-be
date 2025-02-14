package com.carrot.Carrot.service;

import com.carrot.Carrot.model.Operazione;
import com.carrot.Carrot.model.User;
import com.carrot.Carrot.repository.OperazioneRepository;
import com.carrot.Carrot.security.MyUserDetails; // Assicurati che questo sia il tuo custom UserDetails
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OperazioneService {

    private final OperazioneRepository operazioneRepository;

    public OperazioneService(OperazioneRepository operazioneRepository) {
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

    // Restituisce tutte le operazioni appartenenti all'utente corrente, ordinate per data in ordine decrescente
    public List<Operazione> getAllOperazioni() {
        Long currentUserId = getCurrentUser().getId();
        return operazioneRepository.findAllByUserIdOrderByDataOperazioneDesc(currentUserId);
    }

    // Restituisce l'operazione cercata solo se appartiene all'utente corrente
    public Optional<Operazione> getOperazioneById(Long id) {
        Long currentUserId = getCurrentUser().getId();
        return operazioneRepository.findByIdAndUserId(id, currentUserId);
    }

    // Aggiunge una nuova operazione, associandola all'utente corrente
    public void addOperazione(Operazione operazione) {
        operazione.setUser(getCurrentUser()); // Associa l'operazione all'utente corrente
        operazioneRepository.save(operazione);
    }

    // Aggiorna l'operazione solo se appartiene all'utente corrente
    public void updateOperazione(Operazione operazione) {
        Long currentUserId = getCurrentUser().getId();
        if (operazioneRepository.existsByIdAndUserId(operazione.getId(), currentUserId)) {
            operazioneRepository.save(operazione);
        }
    }

    // Elimina l'operazione solo se appartiene all'utente corrente
    public void deleteOperazione(Long id) {
        Long currentUserId = getCurrentUser().getId();
        if (operazioneRepository.existsByIdAndUserId(id, currentUserId)) {
            operazioneRepository.deleteById(id);
        }
    }

    // Restituisce le operazioni recenti per l'utente corrente, limitate al numero richiesto
    public List<Operazione> getOperazioniRecenti(int numeroOperazioni) {
        Pageable pageable = PageRequest.of(0, numeroOperazioni);
        Long currentUserId = getCurrentUser().getId();
        return operazioneRepository.findTopByUserIdOrderByDataOperazioneDesc(currentUserId, pageable);
    }
}
