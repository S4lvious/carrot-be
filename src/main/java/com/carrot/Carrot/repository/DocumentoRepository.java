package com.carrot.Carrot.repository;

import com.carrot.Carrot.model.Documento;
import com.carrot.Carrot.model.Ordine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {
    
    // Trova documenti per ordine
    List<Documento> findByOrdine(Ordine ordine);
    
    // Trova documenti per ordine e utente specifico
    List<Documento> findByOrdineIdAndUserId(Long ordineId, Long userId);
    
    // Trova documenti per cliente e utente specifico
    List<Documento> findByClienteIdAndUserId(Long clienteId, Long userId);
}
