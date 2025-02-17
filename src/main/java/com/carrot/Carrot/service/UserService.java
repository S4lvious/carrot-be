package com.carrot.Carrot.service;

import com.carrot.Carrot.model.User;
import com.carrot.Carrot.repository.UserRepository;
import com.carrot.Carrot.security.MyUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


            private User getCurrentUser() {
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder
                                        .getContext()
                                        .getAuthentication()
                                        .getPrincipal();
        return userDetails.getUser();
    }



    public User getUser() {
        return userRepository.findById(getCurrentUser().getId()).get();
    }


    public boolean esisteUtente() {
        return userRepository.count() > 0;
    }

    public void salvaUtente(User utente) {
        if (!esisteUtente()) {
            userRepository.save(utente);
        } else {
            Optional<User> existingUser = userRepository.findFirstByOrderByIdAsc();
            existingUser.ifPresent(user -> {
                user.setNome(utente.getNome());
                user.setCognome(utente.getCognome());
                user.setRagioneSociale(utente.getRagioneSociale());
                user.setCodiceFiscale(utente.getCodiceFiscale());
                user.setPartitaIva(utente.getPartitaIva());
                user.setIndirizzo(utente.getIndirizzo());
                user.setCap(utente.getCap());
                user.setCitta(utente.getCitta());
                user.setProvincia(utente.getProvincia());
                user.setPec(utente.getPec());
                user.setCodiceDestinatario(utente.getCodiceDestinatario());
                user.setTelefono(utente.getTelefono());
                user.setEmail(utente.getEmail());
                user.setIban(utente.getIban());

                userRepository.save(user);
            });
        }
    }

    public Optional<User> getUtente() {
        return userRepository.findFirstByOrderByIdAsc();
    }
}
