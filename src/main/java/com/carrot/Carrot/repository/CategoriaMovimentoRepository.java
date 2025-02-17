package com.carrot.Carrot.repository;

import com.carrot.Carrot.model.CategoriaMovimento;
import com.carrot.Carrot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaMovimentoRepository extends JpaRepository<CategoriaMovimento, Long> {

    // Trova tutte le categorie di un utente specifico
    List<CategoriaMovimento> findByUser(User user);

    // Conta quante categorie ha un determinato utente
    long countByUser(User user);
}
