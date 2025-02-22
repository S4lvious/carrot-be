package com.carrot.Carrot.repository;

import com.carrot.Carrot.model.Appuntamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppuntamentoRepository extends JpaRepository<Appuntamento, Long> {
    List<Appuntamento> findByUser_Id(Long userId);
    List<Appuntamento> findByClienteId(Long clienteId);
    List<Appuntamento> findByPartecipanti_Id(Long userId);
    List<Appuntamento> findByDataBetweenAndPartecipanti_Id(LocalDate start, LocalDate end, Long userId);
}
