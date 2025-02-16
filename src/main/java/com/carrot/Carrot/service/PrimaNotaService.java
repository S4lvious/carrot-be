package com.carrot.Carrot.service;

import com.carrot.Carrot.enumerator.TipoMovimento;
import com.carrot.Carrot.model.PrimaNota;
import com.carrot.Carrot.model.User;
import com.carrot.Carrot.repository.PrimaNotaRepository;
import com.carrot.Carrot.security.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class PrimaNotaService {

    @Autowired
    private PrimaNotaRepository primaNotaRepository;

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
}
