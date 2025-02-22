package com.carrot.Carrot.repository;
import com.carrot.Carrot.model.AgendaEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AgendaEntryRepository extends JpaRepository<AgendaEntry, Long> {

// ✅ Trova tutte le entry di un determinato utente in un certo intervallo di tempo
    @Query("SELECT ae FROM AgendaEntry ae WHERE ae.dataInizio BETWEEN :startDate AND :endDate AND ae.agenda.user.id = :userId")
    List<AgendaEntry> findByDataInizioBetweenAndAgenda_User_Id(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate, 
        @Param("userId") Long userId);

    // ✅ Trova tutte le entry di tipo "Task" di un utente
    @Query("SELECT ae FROM AgendaEntry ae WHERE ae.task IS NOT NULL AND ae.agenda.user.id = :userId")
    List<AgendaEntry> findByTaskIsNotNullAndAgenda_User_Id(@Param("userId") Long userId);

    // ✅ Trova tutte le entry di tipo "Appuntamento" di un utente
    @Query("SELECT ae FROM AgendaEntry ae WHERE ae.appuntamento IS NOT NULL AND ae.agenda.user.id = :userId")
    List<AgendaEntry> findByAppuntamentoIsNotNullAndAgenda_User_Id(@Param("userId") Long userId);
}
