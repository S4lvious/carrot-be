// src/main/java/com/example/demo/security/MyUserDetailsService.java
package com.carrot.Carrot.security;

import com.carrot.Carrot.model.User;
import com.carrot.Carrot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Cerchiamo l'utente per email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato con email: " + email));
        return new MyUserDetails(user);
    }
    
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                     .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato con id: " + id));
        return new MyUserDetails(user);
    }
}
