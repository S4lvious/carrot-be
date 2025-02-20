package com.carrot.Carrot.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // Usiamo UUID per scalabilità
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String nome; // Nome completo se è un professionista, oppure Nome del Referente dell'azienda.

    private String cognome; // Solo per professionisti

    private String ragioneSociale; // Se è un'azienda

    @Column(nullable = false, unique = true)
    private String codiceFiscale; // Obbligatorio per tutti

    private String partitaIva; // Solo per aziende

    @Column(nullable = false)
    private String indirizzo;

    @Column(nullable = false)
    private String cap;

    @Column(nullable = false)
    private String citta;

    @Column(nullable = false)
    private String provincia;

    private String pec; // Per la fatturazione elettronica

    private String codiceDestinatario; // Per l'XML SDI

    private String telefono;

    @Column(nullable = false, unique = true)
    private String email;

    private String iban; // Per pagamenti

    @Column(nullable = false)
    private boolean enabled = false; // False finché l'utente non verifica l'email

    @Column(nullable = false)
    private boolean trialActive = true; // Se l'utente è ancora nel periodo di prova

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER; // Ruolo dell'utente

    private String requisitionId;

    private String goCardlessRef;


    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Subscription subscription; // Collega la sottoscrizione dell'utente

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "utente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BankAccountsUser> bankAccounts;


    public boolean isAzienda() {
        return ragioneSociale != null && !ragioneSociale.trim().isEmpty();
    }

    public enum Role {
        USER, ADMIN
    }
}
