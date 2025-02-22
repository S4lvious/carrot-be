package com.carrot.Carrot.repository;

import com.carrot.Carrot.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProgettoId(Long progettoId);
    List<Task> findByAssegnatoA_Id(Long userId);
    List<Task> findByStatoAndAssegnatoA_Id(String stato, Long userId);
}
