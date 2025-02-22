package com.carrot.Carrot.repository;

import com.carrot.Carrot.model.Progetto;
import com.carrot.Carrot.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProgettoRepository extends JpaRepository<Progetto, Long> {
    List<Progetto> findByOrdineId(Long ordineId); // Un progetto appartiene a un ordine
    List<Progetto> findByPartecipantiId(Long userId);
    @Query("SELECT u FROM User u WHERE u.id IN (SELECT p FROM Progetto p WHERE p.id = :progettoId)")
    List<User> findPartecipantiByProgettoId(@Param("progettoId") Long progettoId);

}
