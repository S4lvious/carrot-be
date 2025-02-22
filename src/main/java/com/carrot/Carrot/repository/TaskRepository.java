package com.carrot.Carrot.repository;

import com.carrot.Carrot.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProgetto_Id(Long progettoId);
    @Query("SELECT t FROM Task t JOIN t.assegnatoA u WHERE u.id = :userId")
    List<Task> findByAssegnatoA_Id(Long userId);
    @Query("SELECT t FROM Task t JOIN t.assegnatoA u WHERE t.stato = :stato AND u.id = :userId")
    List<Task> findByStatoAndAssegnatoA_Id(String stato, Long userId);
}
