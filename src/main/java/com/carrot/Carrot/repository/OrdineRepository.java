package com.carrot.Carrot.repository;

import com.carrot.Carrot.model.Ordine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdineRepository extends JpaRepository<Ordine, Long> {
    List<Ordine> findByUserId(Long userId);
    Optional<Ordine> findByIdAndUserId(Long id, Long userId);
    List<Ordine> findByFatturatoAndUserId(boolean fatturato, Long userId);
    Long countByUserId(Long userId);
    List<Ordine> findTop5ByUserIdOrderByDataOrdineDesc(Long currentUserId);
    @Query("SELECT COUNT(o) FROM Ordine o WHERE o.user.id = :userId AND YEAR(o.dataOrdine) = :anno")
    int countByUserAndYear(@Param("userId") Long userId, @Param("anno") int anno);

    }
