package com.carrot.Carrot.repository;
import com.carrot.Carrot.model.AgendaEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AgendaEntryRepository extends JpaRepository<AgendaEntry, Long> {

    // ✅ Trova tutte le entry in un certo intervallo di tempo per un determinato utente
    List<AgendaEntry> findByDataBetweenAndUser_Id(LocalDate startDate, LocalDate endDate, Long userId);
    
    // ✅ Trova tutte le entry di un determinato tipo (es. "Task" o "Appuntamento") per un utente
    List<AgendaEntry> findByTipoAndUser_Id(String tipo, Long userId);
}
