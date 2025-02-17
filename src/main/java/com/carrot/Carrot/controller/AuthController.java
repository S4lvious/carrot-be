// src/main/java/com/carrot/Carrot/controller/AuthController.java
package com.carrot.Carrot.controller;

import com.carrot.Carrot.security.JwtTokenProvider;
import com.carrot.Carrot.service.AuthService;

import jakarta.mail.MessagingException;

import com.carrot.Carrot.payload.LoginRequest;
import com.carrot.Carrot.model.User;
import com.carrot.Carrot.payload.JwtAuthenticationResponse;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new JwtAuthenticationResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user, 
                                               @RequestParam(required = false) UUID planId) 
                                               throws MessagingException {
        authService.registerUser(user, planId);
        return ResponseEntity.ok("Registrazione completata! Controlla la tua email per verificare l'account.");
    }
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        String messageOrUrl = authService.verifyEmail(token);
        return ResponseEntity.ok(messageOrUrl);
    }
    
}
