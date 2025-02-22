package com.carrot.Carrot.repository;
import com.carrot.Carrot.model.Agenda;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AgendaRepository extends JpaRepository<Agenda, Long> {
    List<Agenda> findByUser_Id(Long userId);
    List<Agenda> findByDataBetweenAndUser_Id(LocalDate startDate, LocalDate endDate, Long userId);
}
