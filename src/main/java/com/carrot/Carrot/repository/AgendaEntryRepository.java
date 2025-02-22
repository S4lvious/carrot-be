package com.carrot.Carrot.repository;
import com.carrot.Carrot.model.AgendaEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface AgendaEntryRepository extends JpaRepository<AgendaEntry, Long> {

// ✅ Trova tutte le entry di un determinato utente in un certo intervallo di tempo
List<AgendaEntry> findByDataInizioBetweenAndAgenda_User_Id(LocalDateTime startDate, LocalDateTime endDate, Long userId);

// ✅ Trova tutte le entry di un determinato tipo (es. "Task" o "Appuntamento") per un utente
List<AgendaEntry> findByTaskIsNotNullAndAgenda_User_Id(Long userId);
List<AgendaEntry> findByAppuntamentoIsNotNullAndAgenda_User_Id(Long userId);
}
