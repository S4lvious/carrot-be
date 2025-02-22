package com.carrot.Carrot.repository;

import com.carrot.Carrot.model.Appuntamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppuntamentoRepository extends JpaRepository<Appuntamento, Long> {

    // ✅ Trova gli appuntamenti creati da un organizzatore
    List<Appuntamento> findByOrganizzatore_Id(Long userId);

    // ✅ Trova gli appuntamenti legati a un cliente
    List<Appuntamento> findByCliente_Id(Long clienteId);

    // ✅ Trova gli appuntamenti a cui un utente partecipa (CORRETTO)
    @Query("SELECT a FROM Appuntamento a JOIN a.partecipanti p WHERE p.id = :userId")
    List<Appuntamento> findByPartecipanti_Id(@Param("userId") Long userId);

    // ✅ Trova appuntamenti tra due date dove l'utente è partecipante (CORRETTO)
    @Query("SELECT a FROM Appuntamento a JOIN a.partecipanti p WHERE a.dataInizio BETWEEN :start AND :end AND p.id = :userId")
    List<Appuntamento> findByDataInizioBetweenAndPartecipanti_Id(
        @Param("start") LocalDateTime start, 
        @Param("end") LocalDateTime end, 
        @Param("userId") Long userId);
}
