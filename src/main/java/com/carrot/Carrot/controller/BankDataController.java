package com.carrot.Carrot.controller;

import com.carrot.Carrot.dto.*;
import com.carrot.Carrot.enumerator.TipoMovimento;
import com.carrot.Carrot.model.BankAccountsUser;
import com.carrot.Carrot.model.PrimaNota;
import com.carrot.Carrot.model.User;
import com.carrot.Carrot.repository.BankAccountRepository;
import com.carrot.Carrot.repository.PrimaNotaRepository;
import com.carrot.Carrot.repository.UserRepository;
import com.carrot.Carrot.security.MyUserDetails;
import com.carrot.Carrot.service.GoCardlessBankDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bank")
public class BankDataController {

    @Autowired
    private GoCardlessBankDataService bankDataService;

    @Autowired
    private UserRepository utenteRepository;

    @Autowired
    private PrimaNotaRepository primaNotaRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;


        private Long getCurrentUserId() {
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder
                                        .getContext()
                                        .getAuthentication()
                                        .getPrincipal();
        return userDetails.getUser().getId();
    }


    /**
     * (Endpoint #1) - Ritorna la lista di banche per un certo paese
     * Viene chiamato dal frontend Angular per popolare un dropdown
     */
    @GetMapping("/institutions")
    public List<InstitutionDTO> getInstitutions(@RequestParam String country) {
        return bankDataService.getInstitutions(country);
    }

    /**
     * (Endpoint #2) - Crea un requisition e ritorna un link di collegamento
     * L'utente su Angular sceglie la banca e chiama questo endpoint
     */
    @PostMapping("/requisition")
    public RequisitionResponse createRequisition(@RequestBody CreateRequisitionRequest req) {
        // Per semplicità, immaginiamo che l'utente loggato abbia ID=1
        // In un contesto reale, recupereresti l'ID utente dallo JWT o dalla sessione
        User utente = utenteRepository.findById(getCurrentUserId())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        // Se vuoi usare un End User Agreement personalizzato, puoi chiamare:
        // EndUserAgreementResponse agreement = bankDataService.createEndUserAgreement(
        //   req.getInstitutionId(), 180, 30, Arrays.asList("balances","details","transactions")
        // );
        // e poi passare agreement.getId() a createRequisition.

        RequisitionResponse resp = bankDataService.createRequisition(
                req.getRedirectUrl(),
                req.getInstitutionId(),
                req.getReference(),
                null,    // se NON crei un agreement custom
                req.getUserLanguage()
        );

        // Salvo il requisitionId sull'utente
        utente.setRequisitionId(resp.getId());
        utente.setGoCardlessRef(req.getReference());
        utenteRepository.save(utente);

        return resp;
    }

    @PostMapping("/syncData") 
    public ResponseEntity<TransactionsResponse> handleSync(@RequestBody String accountId) {
        return ResponseEntity.ok(this.bankDataService.getTransactions(accountId));  

    }

    @GetMapping("/redirect")
    public ResponseEntity<String> handleRedirect(@RequestParam("ref") String ref) {
    
        // 1) Recupero l'utente in base a goCardlessRef
        User utente = utenteRepository.findByGoCardlessRef(ref)
                .orElseThrow(() -> new RuntimeException("Utente non trovato per goCardlessRef=" + ref));
    
        // 2) Chiamo GoCardless per sapere i conti associati
        RequisitionDetails details = bankDataService.getRequisitionDetails(utente.getRequisitionId());
        List<String> accountIds = details.getAccounts();
    
        // 3) Per ogni accountId, salvo o aggiorno nella tabella 'utente_bank_account'
        if (accountIds != null && !accountIds.isEmpty()) {
            for (String accountId : accountIds) {
                Optional<BankAccountsUser> existing = bankAccountRepository.findByBankAccountId(accountId);
                if (existing.isEmpty()) {
                    BankAccountsUser uba = new BankAccountsUser();
                    uba.setUtente(utente);
                    uba.setBankAccountId(accountId);
                    bankAccountRepository.save(uba);
                }
            }
        }
    
        // 4) Per ogni accountId, aggiorno i dettagli del conto e salvo le transazioni
        if (accountIds != null) {
            for (String accountId : accountIds) {
                try {
                    // Recupera i dettagli del conto
                    var accountDetails = bankDataService.getAccountDetails(accountId);
                    Optional<BankAccountsUser> existing = bankAccountRepository.findByBankAccountId(accountId);
                    if (existing.isPresent()) {
                        BankAccountsUser bankAccount = existing.get();
                        bankAccount.setIban(accountDetails.getAccount().getIban());
                        bankAccount.setOwnerName(accountDetails.getAccount().getOwnerName());
                        bankAccount.setCurrency(accountDetails.getAccount().getCurrency());
                        bankAccount.setAccountName(accountDetails.getAccount().getProduct());
                        bankAccountRepository.save(bankAccount);
                    }
        
                    // Recupera le transazioni per il conto
                    var txResp = bankDataService.getTransactions(accountId);
                    var booked = txResp.getTransactions().getBooked();  // movimenti contabilizzati
                    if (booked != null) {
                        booked.forEach(tx -> {
                            if (!primaNotaRepository.existsByBankTransactionId(tx.getTransactionId())) {
                                PrimaNota pn = new PrimaNota();
                                pn.setUser(utente);
                                pn.setBankTransactionId(tx.getTransactionId());
                                pn.setNome(tx.getRemittanceInformationUnstructured());
                                
                                BigDecimal importo = new BigDecimal(tx.getTransactionAmount().getAmount());
                                pn.setImporto(importo);
                                
                                // Imposta il tipo di movimento in base al segno dell'importo
                                pn.setTipoMovimento(importo.compareTo(BigDecimal.ZERO) < 0 ? TipoMovimento.USCITA : TipoMovimento.ENTRATA);
                            
                                if (tx.getBookingDate() != null) {
                                    pn.setDataOperazione(LocalDate.parse(tx.getBookingDate()));
                                }
                            
                                primaNotaRepository.save(pn);
                            }
                        });
                    }
                } catch (HttpClientErrorException ex) {
                    System.err.println("Accesso negato per l'account " + accountId + ": " + ex.getMessage());
                }
            }
        }
    
        // 5) Redirect finale al front-end della contabilità
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("https://app.powerwebsoftware.it/contabilita"))
                .build();
    }


}

