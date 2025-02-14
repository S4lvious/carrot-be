package com.carrot.Carrot.repository;

import com.carrot.Carrot.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByUserId(Long userId);
    Optional<Cliente> findByIdAndUserId(Long id, Long userId);
    Optional<Cliente> findByPartitaIva(String partitaIva);
    Optional<Cliente> findByCodiceFiscale(String codiceFiscale);
    List<Cliente> findTop5ByOrderByIdDesc();
    List<Cliente>findTop5ByUserIdOrderByIdDesc(Long currentUserId);
}
