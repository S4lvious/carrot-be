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

import java.math.BigDecimal;
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
        utenteRepository.save(utente);

        return resp;
    }

    @GetMapping("/redirect")
    public ResponseEntity<String> handleRedirect(@RequestParam("requisition_id") String requisitionId) {

        // 1) Recupero l'utente in base a requisitionId
        User utente = utenteRepository.findByRequisitionId(requisitionId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato per requisitionId=" + requisitionId));

        // 2) Chiamo GoCardless per sapere i conti associati
        RequisitionDetails details = bankDataService.getRequisitionDetails(requisitionId);
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

        // 4) Recupero le transazioni per ciascun accountId e le salvo in PrimaNota
        if (accountIds != null) {
            for (String accountId : accountIds) {
                var txResp = bankDataService.getTransactions(accountId);

                var booked = txResp.getTransactions().getBooked();  // movimenti contabilizzati
                if (booked != null) {
                    booked.forEach(tx -> {
                        // Evita duplicati
                        if (!primaNotaRepository.existsByBankTransactionId(tx.getTransactionId())) {
                            PrimaNota pn = new PrimaNota();
                            pn.setUser(utente);
                            pn.setBankTransactionId(tx.getTransactionId());
                            pn.setNome(tx.getRemittanceInformationUnstructured());
                            
                            BigDecimal importo = new BigDecimal(tx.getTransactionAmount().getAmount());
                            pn.setImporto(importo);
                            
                            // Imposta il tipo di movimento
                            pn.setTipoMovimento(importo.compareTo(BigDecimal.ZERO) < 0 ? TipoMovimento.USCITA : TipoMovimento.ENTRATA);
                        
                            if (tx.getBookingDate() != null) {
                                pn.setDataOperazione(LocalDate.parse(tx.getBookingDate()));
                            }
                        
                            primaNotaRepository.save(pn);
                        }
                    });
                }
            }
        }

        return ResponseEntity.ok("Conti e transazioni salvati con successo!");
    }
}

