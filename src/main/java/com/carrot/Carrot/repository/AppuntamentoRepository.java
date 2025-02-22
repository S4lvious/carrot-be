package com.carrot.Carrot.repository;

import com.carrot.Carrot.model.Appuntamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppuntamentoRepository extends JpaRepository<Appuntamento, Long> {
    List<Appuntamento> findByOrganizzatore_Id(Long userId);
    List<Appuntamento> findByClienteId(Long clienteId);
    List<Appuntamento> findByPartecipanti_Id(Long userId);
    List<Appuntamento> findByDataInizioBetweenAndPartecipanti_Id(LocalDateTime start, LocalDateTime end, Long userId);
    List<Appuntamento> findByCliente_Id(Long clienteId);
}
