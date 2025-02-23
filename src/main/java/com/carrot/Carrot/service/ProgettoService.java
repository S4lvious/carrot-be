package com.carrot.Carrot.service;

import com.carrot.Carrot.model.Ordine;
import com.carrot.Carrot.model.Progetto;
import com.carrot.Carrot.model.User;
import com.carrot.Carrot.repository.ProgettoRepository;
import com.carrot.Carrot.repository.UserRepository;
import com.carrot.Carrot.security.MyUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProgettoService {

    private final ProgettoRepository progettoRepository;
    @Autowired
    private UserRepository userRepository;

    public ProgettoService(ProgettoRepository progettoRepository, UserRepository userRepository) {
        this.progettoRepository = progettoRepository;
    }

        private User getCurrentUser() {
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder
                                        .getContext()
                                        .getAuthentication()
                                        .getPrincipal();
        return userDetails.getUser();
    }

    // 📌 Recupera i progetti di un utente
    public List<Progetto> getProgettiByUser() {
        User user = getCurrentUser();
        return progettoRepository.findByPartecipanti_Id(user.getId());
    }

    // 📌 Aggiunge un progetto
    @Transactional
    public Progetto addProgetto(Progetto progetto) {
        Long userId = getCurrentUser().getId();
        Optional<User> user = userRepository.findById(userId);
        progetto.getPartecipanti().add(user.get());
        return progettoRepository.save(progetto);
    }

    public Optional<Progetto> getProgettoById(Long progettoId) {
        return progettoRepository.findById(progettoId);
    }

    public Progetto associateToOrder(Ordine ordine, Progetto progetto) {
        progetto.setOrdine(ordine);
        return progettoRepository.save(progetto);

    }

    // 📌 Elimina un progetto
    @Transactional
    public void deleteProgetto(Long progettoId) {
        progettoRepository.deleteById(progettoId);
    }
}
