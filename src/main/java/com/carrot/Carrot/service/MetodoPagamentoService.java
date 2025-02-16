package com.carrot.Carrot.service;

import com.carrot.Carrot.model.MetodoPagamento;
import com.carrot.Carrot.model.User;
import com.carrot.Carrot.repository.MetodoPagamentoRepository;
import com.carrot.Carrot.security.MyUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class MetodoPagamentoService {

    private final MetodoPagamentoRepository metodoPagamentoRepository;

    public MetodoPagamentoService(MetodoPagamentoRepository metodoPagamentoRepository) {
        this.metodoPagamentoRepository = metodoPagamentoRepository;
    }

    // Ottenere l'utente autenticato
    private User getCurrentUser() {
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    // Ottenere tutti i metodi di pagamento dell'utente autenticato
    public List<MetodoPagamento> getAllMetodiPagamento() {
        return metodoPagamentoRepository.findByUserId(getCurrentUser().getId());
    }

    // Ottenere un metodo di pagamento specifico
    public Optional<MetodoPagamento> getMetodoPagamentoById(Long id) {
        return metodoPagamentoRepository.findByIdAndUserId(id, getCurrentUser().getId());
    }

    // Creare un nuovo metodo di pagamento
    public MetodoPagamento createMetodoPagamento(MetodoPagamento metodoPagamento) {
        metodoPagamento.setUser(getCurrentUser());
        return metodoPagamentoRepository.save(metodoPagamento);
    }

    // Modificare un metodo di pagamento esistente
    public MetodoPagamento updateMetodoPagamento(Long id, MetodoPagamento updatedMetodoPagamento) {
        return metodoPagamentoRepository.findByIdAndUserId(id, getCurrentUser().getId()).map(existing -> {
            existing.setNome(updatedMetodoPagamento.getNome());
            return metodoPagamentoRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Metodo di pagamento non trovato"));
    }

    // Eliminare un metodo di pagamento
    public void deleteMetodoPagamento(Long id) {
        metodoPagamentoRepository.deleteByIdAndUserId(id, getCurrentUser().getId());
    }
}