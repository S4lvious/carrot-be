package com.carrot.Carrot.repository;
import com.carrot.Carrot.model.Agenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AgendaRepository extends JpaRepository<Agenda, Long> {
    List<Agenda> findByUser_Id(Long userId);
    @Query("SELECT a FROM Agenda a WHERE a.data BETWEEN :startDate AND :endDate AND a.user.id = :userId")
    List<Agenda> findByDataBetweenAndUser_Id(@Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate, 
                                             @Param("userId") Long userId);
}
