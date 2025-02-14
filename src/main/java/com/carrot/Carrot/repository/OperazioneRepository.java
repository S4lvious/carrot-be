package com.carrot.Carrot.repository;

import com.carrot.Carrot.model.Operazione;

import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OperazioneRepository extends JpaRepository<Operazione, Long> {
    List<Operazione> findAllByUserIdOrderByDataOperazioneDesc(Long userId);
    Optional<Operazione> findByIdAndUserId(Long id, Long userId);
    boolean existsByIdAndUserId(Long id, Long userId);
    List<Operazione> findTopByUserIdOrderByDataOperazioneDesc(Long userId, Pageable pageable);
}
