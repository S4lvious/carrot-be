package com.carrot.Carrot.repository;

import com.carrot.Carrot.model.Categoria;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findAllByUser_Id(Long userId);
    Categoria findByIdAndUser_Id(Long id, Long currentUserId);
}
