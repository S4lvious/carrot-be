package com.carrot.Carrot.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@SuppressWarnings("deprecation")
@Component
public class JwtTokenProvider {

    // Per HS512 è necessaria una chiave lunga (almeno 64 byte)
    @Value("${app.jwtSecret:mySecretKeymySecretKeymySecretKeymySecretKeymySecretKeymySecretKeymySecretKeymySecretKey}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs:86400000}")  // 1 giorno
    private int jwtExpirationInMs;

    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication authentication) {
        MyUserDetails userPrincipal = (MyUserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .subject(Long.toString(userPrincipal.getUser().getId()))
                .claim("username", userPrincipal.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Long getUserIdFromJWT(String token) {
        Claims claims = Jwts.parser()     // Metodo non deprecato se hai le dipendenze corrette
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(authToken);
            return true;
        } catch (Exception ex) {
            // Loggare o gestire l'errore come necessario
        }
        return false;
    }
}
