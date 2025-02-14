package com.carrot.Carrot.service;

import com.carrot.Carrot.model.Cliente;
import com.carrot.Carrot.model.Operazione;
import com.carrot.Carrot.model.User;
import com.carrot.Carrot.repository.ClienteRepository;
import com.carrot.Carrot.repository.OperazioneRepository;
import com.carrot.Carrot.security.MyUserDetails;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final OperazioneRepository operazioneRepository;

    public ClienteService(ClienteRepository clienteRepository, OperazioneRepository operazioneRepository) {
        this.clienteRepository = clienteRepository;
        this.operazioneRepository = operazioneRepository;
        System.out.println("🔹 ClienteService istanziato correttamente!");
    }

    public List<Cliente> getAllClients() {
            MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder
                                    .getContext()
                                    .getAuthentication()
                                    .getPrincipal();
    Long currentUserId = userDetails.getUser().getId();

        return clienteRepository.findByUserId(currentUserId);
    }

    public Optional<Cliente> getClientById(Long id) {
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();
Long currentUserId = userDetails.getUser().getId();

        return clienteRepository.findByIdAndUserId(id, currentUserId);
    }

    @Transactional
    public void addClient(Cliente cliente) {
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();
        User user = userDetails.getUser();
        cliente.setUser(user);
        clienteRepository.save(cliente);
        operazioneRepository.save(new Operazione(
                "Cliente", 
                "Aggiunta", 
                "Nuovo cliente aggiunto: " + cliente.getNome() + " " + cliente.getCognome(), 
                LocalDateTime.now(),
                user
        ));
    }

    @Transactional
    public void updateClient(Cliente cliente) {
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();
        User user = userDetails.getUser();
        cliente.setUser(user);
        clienteRepository.save(cliente);
        operazioneRepository.save(new Operazione(
                "Cliente",
                "Modifica",
                "Cliente aggiornato: " + cliente.getNome() + " " + cliente.getCognome(),
                LocalDateTime.now(),
                user
        ));
    }

    @Transactional
    public void deleteClient(Long id) {
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();
        User user = userDetails.getUser();
        Optional<Cliente> clienteOpt = clienteRepository.findByIdAndUserId(id, user.getId());
        clienteOpt.ifPresent(cliente -> {
            clienteRepository.delete(cliente);
            operazioneRepository.save(new Operazione(
                    "Cliente",
                    "Eliminazione",
                    "Cliente eliminato: " + cliente.getNome() + " " + cliente.getCognome(),
                    LocalDateTime.now(),
                    user
            ));
        });
    }
}
