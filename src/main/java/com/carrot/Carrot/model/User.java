package com.carrot.Carrot.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    private String email;

    private String iban; // Per pagamenti

    public boolean isAzienda() {
        return ragioneSociale != null && !ragioneSociale.trim().isEmpty();
    }
}
