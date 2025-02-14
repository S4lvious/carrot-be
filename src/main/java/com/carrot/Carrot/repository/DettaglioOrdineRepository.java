package com.carrot.Carrot.repository;

import com.carrot.Carrot.model.DettaglioOrdine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DettaglioOrdineRepository extends JpaRepository<DettaglioOrdine, Long> {
    List<DettaglioOrdine> findByOrdineId(Long ordineId);
}
