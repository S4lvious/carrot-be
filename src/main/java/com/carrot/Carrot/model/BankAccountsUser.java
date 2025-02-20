package com.carrot.Carrot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "utente_bank_account")
public class BankAccountsUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relazione con l'utente
    @ManyToOne
    @JoinColumn(name = "utente_id")
    private User utente;

    // L'ID del conto restituito dalle API di GoCardless (Account ID)
    @Column(nullable = false, unique = true)
    private String bankAccountId;

    private String iban;
    private String bic;
    private String currency;     
    private String accountName;  
    private String ownerName;     
    private LocalDateTime createdAt = LocalDateTime.now();

}
