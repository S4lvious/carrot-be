package com.carrot.Carrot.controller;

import com.carrot.Carrot.model.User;
import com.carrot.Carrot.repository.UserRepository;
import com.carrot.Carrot.service.UserService;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {


    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> esisteUtente() {
        return ResponseEntity.ok(userService.esisteUtente());
    }

    @GetMapping("/getUser")
    public ResponseEntity<User> getUser() {
        return ResponseEntity.ok(userService.getUser());
    }

    @GetMapping("/profile-status")
    public ResponseEntity<Map<String, Boolean>> getProfileStatus() {
        User user = userService.getUser();
        Map<String, Boolean> response = new HashMap<>();
        response.put("profileCompleted", user.isTrialActive());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/complete-profile")
    public ResponseEntity<String> completeUserProfile(@RequestBody User profileDTO) {
        User user = userService.getUser();
        user.setUsername(profileDTO.getUsername());
        user.setNome(profileDTO.getNome());
        user.setCognome(profileDTO.getCognome());
        user.setRagioneSociale(profileDTO.getRagioneSociale());
        user.setCodiceFiscale(profileDTO.getCodiceFiscale());
        user.setPartitaIva(profileDTO.getPartitaIva());
        user.setIndirizzo(profileDTO.getIndirizzo());
        user.setCap(profileDTO.getCap());
        user.setCitta(profileDTO.getCitta());
        user.setProvincia(profileDTO.getProvincia());
        user.setPec(profileDTO.getPec());
        user.setCodiceDestinatario(profileDTO.getCodiceDestinatario());
        user.setTelefono(profileDTO.getTelefono());
        user.setEmail(profileDTO.getEmail());
        user.setIban(profileDTO.getIban());
        user.setEnabled(profileDTO.isEnabled());
        user.setTrialActive(profileDTO.isTrialActive());
        user.setRole(profileDTO.getRole());
        user.setTrialActive(true);
        userRepository.save(user);
        return ResponseEntity.ok("Profilo completato con successo");
    }
    



    @GetMapping
    public ResponseEntity<User> getUtente() {
        return userService.getUtente().map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Void> salvaUtente(@RequestBody User user) {
        userService.salvaUtente(user);
        return ResponseEntity.ok().build();
    }
}