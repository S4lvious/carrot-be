package com.carrot.Carrot.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "clienti")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String ragioneSociale;
    
    @Column(nullable = true)
    private String nome;
    
    @Column(nullable = true)
    private String cognome;
    
    private String email;
    private String telefono;
    
    @Column(nullable = true)
    private String codiceFiscale;
    
    private String partitaIva;
    
    private String indirizzo;
    private String citta;
    private String provincia;
    private String cap;
    
    // Nuovi campi per le API
    @Column(name = "codice_sdi")
    private String codiceSDI;
    private String pec;
    private String nazione; // Codice ISO, es. "IT"
    
    @Lob
    private String note;

    private LocalDate dataUltimoOrdine;

    @Transient
    @JsonProperty("isAzienda")
    public boolean isAzienda() {
        return ragioneSociale != null && !ragioneSociale.trim().isEmpty();
    }
}
