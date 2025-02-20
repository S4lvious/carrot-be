package com.carrot.Carrot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Service
public class GoCardlessBankDataAuthService {

    @Value("${gocardless.bad.secret-id}")
    private String secretId;

    @Value("${gocardless.bad.secret-key}")
    private String secretKey;

    @Value("${gocardless.bad.api-url}")
    private String baseUrl;

    private String currentAccessToken;  
    private String refreshToken;        

    // Esempio: all'avvio prendiamo un token
    // (In un progetto reale potresti farlo in un CommandLineRunner
    //  o su una specifica endpoint di init).
    // Qui lo mostriamo come metodo pubblico.
    public void fetchNewAccessToken() {
        String url = baseUrl + "/token/new/";

        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> body = new HashMap<>();
        body.put("secret_id", secretId);
        body.put("secret_key", secretKey);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<TokenResponse> response =
                restTemplate.exchange(url, HttpMethod.POST, request, TokenResponse.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            TokenResponse tr = response.getBody();
            this.currentAccessToken = tr.getAccess();
            this.refreshToken = tr.getRefresh();
            System.out.println("[AUTH] Ottenuto nuovo access token: " + this.currentAccessToken);
        } else {
            throw new RuntimeException("Impossibile ottenere token da GoCardless!");
        }
    }

    @Scheduled(fixedRate = 82800000) // 23 ore in millisecondi
    public void scheduledRefresh() {
        System.out.println("[SCHEDULER] Sto rinfrescando token");
        fetchNewAccessToken();
    }

    public String getAccessToken() {
        // Eventualmente potresti controllare se scaduto e rifare fetch
        return this.currentAccessToken;
    }

    @Data
    static class TokenResponse {
        private String access;
        private int access_expires;
        private String refresh;
        private int refresh_expires;
    }
}
