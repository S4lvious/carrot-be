// src/main/java/com/example/demo/security/MyUserDetails.java
package com.carrot.Carrot.security;

import com.carrot.Carrot.model.User;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class MyUserDetails implements UserDetails {

    private final User user;

    public MyUserDetails(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public String getEmail() {
        return user.getEmail();
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Restituisci i ruoli o authorities, se previsti
        return null;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    // Gli altri metodi possono restituire true per semplicità
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
