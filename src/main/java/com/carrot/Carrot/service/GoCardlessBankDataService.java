package com.carrot.Carrot.service;

import com.carrot.Carrot.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GoCardlessBankDataService {

    @Autowired
    private GoCardlessBankDataAuthService authService;

    @Value("${gocardless.bad.api-url}")
    private String baseUrl;

    private RestTemplate restTemplate = new RestTemplate();

    /**
     * 1) Restituisce la lista delle banche (institutions) disponibili in un dato paese
     */
    public List<InstitutionDTO> getInstitutions(String countryCode) {
        String endpoint = baseUrl + "/institutions/?country=" + countryCode;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authService.getAccessToken());
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<InstitutionDTO[]> response = restTemplate.exchange(
            endpoint, HttpMethod.GET, requestEntity, InstitutionDTO[].class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return Arrays.asList(response.getBody());
        }
        return Collections.emptyList();
    }

    /**
     * 2) Facoltativo: Creare un end user agreement con parametri custom
     */
    public Object createEndUserAgreement(String institutionId,
                                                           Integer maxHistoricalDays,
                                                           Integer accessValidForDays,
                                                           List<String> accessScope) {
        String endpoint = baseUrl + "/agreements/enduser/";

        Map<String, Object> body = new HashMap<>();
        body.put("institution_id", institutionId);
        if (maxHistoricalDays != null) body.put("max_historical_days", maxHistoricalDays);
        if (accessValidForDays != null) body.put("access_valid_for_days", accessValidForDays);
        if (accessScope != null) body.put("access_scope", accessScope);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authService.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Object> response = restTemplate.exchange(
            endpoint, HttpMethod.POST, requestEntity, Object.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Errore nella creazione dell'end user agreement");
        }

        return response.getBody();
    }

    /**
     * 3) Creare un requisition e ottenere il link
     */
    public RequisitionResponse createRequisition(String redirectUrl,
                                                 String institutionId,
                                                 String reference,
                                                 String agreementId,
                                                 String userLanguage) {
        String endpoint = baseUrl + "/requisitions/";

        Map<String, Object> body = new HashMap<>();
        body.put("redirect", redirectUrl);
        body.put("institution_id", institutionId);
        body.put("reference", reference);
        if (agreementId != null) body.put("agreement", agreementId);
        if (userLanguage != null) body.put("user_language", userLanguage);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authService.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<RequisitionResponse> response = restTemplate.exchange(
            endpoint, HttpMethod.POST, requestEntity, RequisitionResponse.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Errore nella creazione del requisition");
        }

        return response.getBody();
    }

    /**
     * 4) Ottenere dettagli di un requisition, compresa la lista di account
     */
    public RequisitionDetails getRequisitionDetails(String requisitionId) {
        String endpoint = baseUrl + "/requisitions/" + requisitionId + "/";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authService.getAccessToken());
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<RequisitionDetails> response = restTemplate.exchange(
            endpoint, HttpMethod.GET, requestEntity, RequisitionDetails.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Errore nel recupero requisition");
        }
        return response.getBody();
    }


    public AccountDetailsResponse getAccountDetails(String accountId) {
        String endpoint = baseUrl + "/accounts/" + accountId + "/details/";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authService.getAccessToken());
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<AccountDetailsResponse> response = restTemplate.exchange(
            endpoint, HttpMethod.GET, requestEntity, AccountDetailsResponse.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Errore nel recupero dettagli account");
        }
        return response.getBody();

    }

    /**
     * 5) Ottenere le transazioni di un account
     */
    public TransactionsResponse getTransactions(String accountId) {
        String endpoint = baseUrl + "/accounts/" + accountId + "/transactions/";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authService.getAccessToken());
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<TransactionsResponse> response = restTemplate.exchange(
            endpoint, HttpMethod.GET, requestEntity, TransactionsResponse.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Errore nel recupero transazioni");
        }
        return response.getBody();
    }
}
