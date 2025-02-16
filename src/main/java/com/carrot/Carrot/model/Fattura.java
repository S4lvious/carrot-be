package com.carrot.Carrot.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fatture")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Fattura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @OneToOne
    @JoinColumn(name = "ordine_id", nullable = false)
    private Ordine ordine;

    @Column(name = "numero_fattura", nullable = false, unique = true)
    private String numeroFattura; // Es: "2024-001"

    @Column(name = "data_emissione", nullable = false)
    private LocalDate dataEmissione = LocalDate.now();

    @Column(name = "totale_netto", nullable = false)
    private BigDecimal totaleNetto;

    @Column(name = "totale_iva", nullable = false)
    private BigDecimal totaleIVA;

    @Column(name = "totale_lordo", nullable = false)
    private BigDecimal totaleLordo;

    @Column(name = "totale_dovuto", nullable = false)
    private BigDecimal totaleDovuto;

    @Column(name = "applicare_ritenuta", nullable = false)
    private boolean applicareRitenuta;

    @Column(name = "ritenuta_acconto", nullable = false)
    private BigDecimal ritenutaAcconto;

    @Column(name = "inviata_ade", nullable = false)
    private boolean inviataAdE = false;

    @Column(name = "stato", nullable = false)
    private String stato;

    @Column(name = "data_scadenza", nullable = false)
    private LocalDate dataScadenza;

    // **Dati dell'utente emittente**
    @Column(name = "nome_emittente", nullable = false)
    private String nomeEmittente;

    @Column(name = "indirizzo_emittente", nullable = false)
    private String indirizzoEmittente;

    @Column(name = "cap_emittente", nullable = false)
    private String capEmittente;

    @Column(name = "citta_emittente", nullable = false)
    private String cittaEmittente;

    @Column(name = "provincia_emittente", nullable = false)
    private String provinciaEmittente;

    @Column(name = "partita_iva_emittente", nullable = false)
    private String partitaIVAEmittente;

    @Column(name = "codice_fiscale_emittente")
    private String codiceFiscaleEmittente;

    // **Dati del Cliente al momento della fattura**
    @Column(name = "nome_cliente", nullable = false)
    private String nomeCliente;

    @Column(name = "indirizzo_cliente", nullable = false)
    private String indirizzoCliente;

    @Column(name = "cap_cliente", nullable = false)
    private String capCliente;

    @Column(name = "citta_cliente", nullable = false)
    private String cittaCliente;

    @Column(name = "provincia_cliente", nullable = false)
    private String provinciaCliente;

    @Column(name = "partita_iva_cliente")
    private String partitaIVACliente;

    @Column(name = "codice_fiscale_cliente")
    private String codiceFiscaleCliente;
}
