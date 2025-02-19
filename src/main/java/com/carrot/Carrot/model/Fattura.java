package com.carrot.Carrot.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.carrot.Carrot.enumerator.CondizioniPagamento;
import com.carrot.Carrot.enumerator.TipoDocumento;

@Entity
@Table(name = "fatture")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Fattura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Associazioni
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @OneToOne
    @JoinColumn(name = "ordine_id", nullable = false)
    private Ordine ordine;

    // Dati base della fattura
    @Column(name = "numero_fattura", nullable = false)
    private String numeroFattura;

    @Column(name = "data_emissione", nullable = false)
    private LocalDate dataEmissione;

    @Column(name = "totale_netto", nullable = false)
    private BigDecimal totaleNetto;

    @Column(name = "totale_iva", nullable = false)
    private BigDecimal totaleIVA;

    @Column(name = "totale_lordo", nullable = false)
    private BigDecimal totaleLordo;

    @Column(name = "totale_dovuto", nullable = false)
    private BigDecimal totaleDovuto;

    // Ritenuta
    @Column(name = "applicare_ritenuta", nullable = false)
    private boolean applicareRitenuta;

    // Questa è la percentuale di ritenuta (es. 20.00 se è il 20%)
    @Column(name = "ritenuta_acconto", precision = 5, scale = 2)
    private BigDecimal ritenutaAcconto;

    @Column(name = "importo_ritenuta")
    private BigDecimal importoRitenuta;

    @Column(name = "inviata_ade", nullable = false)
    private boolean inviataAdE;

    @Column(name = "stato", nullable = false)
    private String stato;

    @Column(name = "data_scadenza", nullable = false)
    private LocalDate dataScadenza;

    // Dati dell'emittente (mittente)
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

    // Dati del destinatario (cliente)
    @Column(name = "nome_cliente", nullable = false)
    private String nomeCliente; // per uso interno, corrisponde anche a denominazione
    
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

    // Campi aggiuntivi per il destinatario (secondo il JSON)
    @Column(name = "codice_sdi_destinatario")
    private String codiceSDIDestinatario;

    @Column(name = "pec_destinatario")
    private String pecDestinatario;

    @Column(name = "denominazione_destinatario")
    private String denominazioneDestinatario;

    @Column(name = "nazione_destinatario")
    private String nazioneDestinatario; // Codice ISO, es. "IT"

    // Dati del documento
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false)
    private TipoDocumento tipoDocumento;
    
    @Column(name = "causale")
    private String causale;
    
    @Column(name = "causale_pagamento")
    private String causalePagamento;

    // Dati Bollo
    @Embedded
    private DatiBollo datiBollo;

    // Dati Cassa Previdenziale
    @Embedded
    private DatiCassaPrevidenziale datiCassaPrevidenziale;

    // Documenti di riferimento (Ordine, Contratto, Convenzione, Ricezione, Fatture Collegate)
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "idDocumento", column = @Column(name = "id_documento_ordine_acquisto")),
        @AttributeOverride(name = "data", column = @Column(name = "data_ordine_acquisto")),
        @AttributeOverride(name = "codiceCommessaConvenzione", column = @Column(name = "codice_commessa_ordine_acquisto")),
        @AttributeOverride(name = "codiceCUP", column = @Column(name = "codice_cup_ordine_acquisto")),
        @AttributeOverride(name = "codiceCIG", column = @Column(name = "codice_cig_ordine_acquisto"))
    })
    private DocumentoRiferimento datiOrdineAcquisto;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "idDocumento", column = @Column(name = "id_documento_contratto")),
        @AttributeOverride(name = "data", column = @Column(name = "data_contratto")),
        @AttributeOverride(name = "codiceCommessaConvenzione", column = @Column(name = "codice_commessa_contratto")),
        @AttributeOverride(name = "codiceCUP", column = @Column(name = "codice_cup_contratto")),
        @AttributeOverride(name = "codiceCIG", column = @Column(name = "codice_cig_contratto"))
    })
    private DocumentoRiferimento datiContratto;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "idDocumento", column = @Column(name = "id_documento_convenzione")),
        @AttributeOverride(name = "data", column = @Column(name = "data_convenzione")),
        @AttributeOverride(name = "codiceCommessaConvenzione", column = @Column(name = "codice_commessa_convenzione")),
        @AttributeOverride(name = "codiceCUP", column = @Column(name = "codice_cup_convenzione")),
        @AttributeOverride(name = "codiceCIG", column = @Column(name = "codice_cig_convenzione"))
    })
    private DocumentoRiferimento datiConvenzione;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "idDocumento", column = @Column(name = "id_documento_ricezione")),
        @AttributeOverride(name = "data", column = @Column(name = "data_ricezione")),
        @AttributeOverride(name = "codiceCommessaConvenzione", column = @Column(name = "codice_commessa_ricezione")),
        @AttributeOverride(name = "codiceCUP", column = @Column(name = "codice_cup_ricezione")),
        @AttributeOverride(name = "codiceCIG", column = @Column(name = "codice_cig_ricezione"))
    })
    private DocumentoRiferimento datiRicezione;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "idDocumento", column = @Column(name = "id_documento_fatture_collegate")),
        @AttributeOverride(name = "data", column = @Column(name = "data_fatture_collegate")),
        @AttributeOverride(name = "codiceCommessaConvenzione", column = @Column(name = "codice_commessa_fatture_collegate")),
        @AttributeOverride(name = "codiceCUP", column = @Column(name = "codice_cup_fatture_collegate")),
        @AttributeOverride(name = "codiceCIG", column = @Column(name = "codice_cig_fatture_collegate"))
    })
    private DocumentoRiferimento datiFattureCollegate;

    // Dati Pagamento
    @Embedded
    private DatiPagamento datiPagamento;

    // Dati Bollo (presi dall'ordine)
    @Column(name = "bollo_virtuale")
    private Boolean bolloVirtuale;

    @Column(name = "importo_bollo", precision = 10, scale = 2)
    private BigDecimal importoBollo;

    // Dati Cassa Previdenziale
    @Column(name = "tipo_cassa")
    private String tipoCassa;
    
    @Column(name = "al_cassa")
    private String alCassa;
    
    @Column(name = "importo_contributo_cassa", precision = 10, scale = 2)
    private BigDecimal importoContributoCassa;
    
    @Column(name = "imponibile_cassa", precision = 10, scale = 2)
    private BigDecimal imponibileCassa;
    
    @Column(name = "aliquota_iva_cassa", precision = 5, scale = 2)
    private BigDecimal aliquotaIVACassa;
    
    @Column(name = "natura_cassa")
    private String naturaCassa;
    
    @Column(name = "ritenuta_cassa")
    private Boolean ritenutaCassa;

    
    // Dati Pagamento
    @Enumerated(EnumType.STRING)
    @Column(name = "condizioni_pagamento", nullable = false)
    private CondizioniPagamento condizioniPagamento;
    // Dettagli dell'ordine
}
