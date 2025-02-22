package com.carrot.Carrot.service;
import com.carrot.Carrot.model.Agenda;
import com.carrot.Carrot.model.AgendaEntry;
import com.carrot.Carrot.repository.AgendaEntryRepository;
import com.carrot.Carrot.repository.AgendaRepository;
import com.carrot.Carrot.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AgendaService {

    private final AgendaRepository agendaRepository;
    private final AgendaEntryRepository agendaEntryRepository;

    public AgendaService(AgendaRepository agendaRepository, 
                         AgendaEntryRepository agendaEntryRepository,
                         UserRepository userRepository) {
        this.agendaRepository = agendaRepository;
        this.agendaEntryRepository = agendaEntryRepository;
    }

    // 📌 Recupera l'agenda di un utente
    public Optional<List<Agenda>> getAgendaByUser(Long userId) {
        List<Agenda> agenda = agendaRepository.findByUserId(userId);
        return agenda.isEmpty() ? Optional.empty() : Optional.of(agenda);
    }

    // 📌 Recupera tutti gli eventi di un utente in un intervallo di date
    public List<AgendaEntry> getEntriesByDateAndUser(Long userId, LocalDate start, LocalDate end) {
        return agendaEntryRepository.findByDataBetweenAndUser_Id(start, end, userId);
    }

    // 📌 Aggiungi un'entry all'agenda
    @Transactional
    public AgendaEntry addEntry(Agenda agenda, AgendaEntry entry) {
        entry.setAgenda(agenda);
        return agendaEntryRepository.save(entry);
    }

    // 📌 Elimina un'entry dall'agenda
    @Transactional
    public void deleteEntry(Long entryId) {
        agendaEntryRepository.deleteById(entryId);
    }
}
