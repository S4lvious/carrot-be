package com.carrot.Carrot.service;
import com.carrot.Carrot.model.Appuntamento;
import com.carrot.Carrot.model.User;
import com.carrot.Carrot.repository.AppuntamentoRepository;
import com.carrot.Carrot.repository.UserRepository;
import com.carrot.Carrot.security.MyUserDetails;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppuntamentoService {

    private final AppuntamentoRepository appuntamentoRepository;

    public AppuntamentoService(AppuntamentoRepository appuntamentoRepository, UserRepository userRepository) {
        this.appuntamentoRepository = appuntamentoRepository;
    }

                private User getCurrentUser() {
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder
                                        .getContext()
                                        .getAuthentication()
                                        .getPrincipal();
        return userDetails.getUser();
    }


    // 📌 Recupera tutti gli appuntamenti di un utente
    public List<Appuntamento> getAppuntamentiByUser(Long userId) {
        return appuntamentoRepository.findByPartecipanti_Id(userId);
    }

    // 📌 Recupera tutti gli appuntamenti in un intervallo di date
    public List<Appuntamento> getAppuntamentiByDate(Long userId, LocalDateTime start, LocalDateTime end) {
        return appuntamentoRepository.findByDataInizioBetweenAndPartecipanti_Id(start, end, userId);
    }

    // 📌 Aggiungi un appuntamento
    @Transactional
    public Appuntamento addAppuntamento(Long userId, Appuntamento appuntamento) {
        User user = getCurrentUser();
        appuntamento.getPartecipanti().add(user);
        return appuntamentoRepository.save(appuntamento);
    }

    // 📌 Elimina un appuntamento
    @Transactional
    public void deleteAppuntamento(Long appuntamentoId) {
        appuntamentoRepository.deleteById(appuntamentoId);
    }
}
