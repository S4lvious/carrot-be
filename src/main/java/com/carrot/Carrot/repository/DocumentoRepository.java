package com.carrot.Carrot.repository;

import com.carrot.Carrot.model.Documento;
import com.carrot.Carrot.model.Ordine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {
    
    // Recupera tutti i documenti associati a un determinato ordine
    List<Documento> findByOrdine(Ordine ordine);
}
