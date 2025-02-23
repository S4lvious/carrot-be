package com.carrot.Carrot.repository;

import com.carrot.Carrot.model.Progetto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProgettoRepository extends JpaRepository<Progetto, Long> {
    List<Progetto> findByOrdineId(Long ordineId); // Un progetto appartiene a un ordine
    List<Progetto> findByPartecipanti_Id(Long userId);
}
