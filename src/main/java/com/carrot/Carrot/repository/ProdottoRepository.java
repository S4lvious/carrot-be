package com.carrot.Carrot.repository;

import com.carrot.Carrot.model.Prodotto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdottoRepository extends JpaRepository<Prodotto, Long> {
    List<Prodotto> findByUserId(Long userId);
    Optional<Prodotto> findByIdAndUserId(Long id, Long userId);
    List<Prodotto> findByQuantitaLessThanEqualAndEsauribileIsTrueAndUserId(int soglia, Long userId);
    boolean existsByIdAndUserId(Long id, Long userId);
    List<Prodotto> findTop5ByUserIdOrderByIdDesc(Long currentUserId);
    }
