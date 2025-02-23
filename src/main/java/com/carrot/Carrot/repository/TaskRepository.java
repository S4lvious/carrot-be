package com.carrot.Carrot.repository;

import com.carrot.Carrot.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProgetto_Id(Long progettoId);
    @Query("SELECT t FROM Task t JOIN t.assegnatoA u WHERE u.id = :userId")
    List<Task> findByAssegnatoA_Id(Long userId);
    @Query("SELECT t FROM Task t JOIN t.assegnatoA u WHERE t.stato = :stato AND u.id = :userId")
    List<Task> findByStatoAndAssegnatoA_Id(String stato, Long userId);
    @Modifying
    @Query("UPDATE Task t SET t.progetto.id = :newProjectId WHERE t.id = :taskId")
    void updateTaskProject(@Param("taskId") Long taskId, @Param("newProjectId") Long newProjectId);

}
