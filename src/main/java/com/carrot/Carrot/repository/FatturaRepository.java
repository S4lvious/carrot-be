package com.carrot.Carrot.repository;

import com.carrot.Carrot.model.Fattura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FatturaRepository extends JpaRepository<Fattura, Long> {
    Optional<Fattura> findByOrdineId(Long ordineId);
    List<Fattura> findByStato(String stato);
    boolean existsByOrdineId(Long ordineId);
    List<Fattura> findTop5ByOrderByDataEmissioneDesc();
    List<Fattura> findByUserId(Long userId);
    Optional<Fattura> findByIdAndUserId(Long id, Long userId);
    List<Fattura> findByStatoAndUserId(String stato, Long userId);
    boolean existsByOrdineIdAndUserId(Long ordineId, Long userId);
    List<Fattura> findTop5ByUserIdOrderByDataEmissioneDesc(Long currentUserId);


}
