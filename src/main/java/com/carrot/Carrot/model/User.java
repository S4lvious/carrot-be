package com.carrot.Carrot.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "isAzienda"}) // ✅ Ignora "isAzienda" durante la deserializzazione
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // Usiamo UUID per scalabilità
    private Long id;

    @Column(unique = true, nullable = true)
    private String username;

    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String nome; // Nome completo se è un professionista, oppure Nome del Referente dell'azienda.
    @Column(nullable = false)
    private String cognome; // Solo per professionisti

    private String ragioneSociale; // Se è un'azienda

    @Column(nullable = true, unique = true)
    private String codiceFiscale; // Obbligatorio per tutti

    private String partitaIva; // Solo per aziende

    @Column(nullable = true)
    private String indirizzo;

    @Column(nullable = true)
    private String cap;

    @Column(nullable = true)
    private String citta;

    @Column(nullable = true)
    private String provincia;

    private String pec; // Per la fatturazione elettronica

    private String codiceDestinatario; // Per l'XML SDI

    private String telefono;

    @Column(nullable = false, unique = true)
    private String email;

    private String iban; // Per pagamenti

    @Column(nullable = true)
    private boolean enabled = false; // False finché l'utente non verifica l'email

    @Column(nullable = true)
    private boolean trialActive = true; // Se l'utente è ancora nel periodo di prova

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Role role = Role.USER; // Ruolo dell'utente

    private String requisitionId;

    private String goCardlessRef;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Subscription subscription; // Collega la sottoscrizione dell'utente

    @Column(nullable = true)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "utente", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<BankAccountsUser> bankAccounts;

    @JsonProperty("isAzienda")
    public boolean isAzienda() {
        return ragioneSociale != null && !ragioneSociale.trim().isEmpty();
    }

    public enum Role {
        USER, ADMIN
    }
}
