package com.carrot.Carrot.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "prodotti")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Prodotto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String nome;

    // Descrizione estesa del prodotto/servizio
    private String descrizione;

    // Prezzo di listino del prodotto (non sempre uguale a "prezzoUnitario" della singola riga)
    @Column(nullable = false)
    private BigDecimal prezzo;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Categoria categoria;

    // Aliquota IVA standard per questo prodotto
    @Column(name = "aliquota_iva", nullable = false)
    private BigDecimal aliquotaIVA;

    // Quantità a magazzino (se gestisci lo stock)
    @Column(nullable = false)
    private int quantita;

    // Se true, il prodotto è soggetto a decremento di stock
    @Column(nullable = false)
    private boolean esauribile;

    // -------------------------------
    // Campi aggiunti per la Fattura Elettronica (linea di dettaglio)
    // -------------------------------

    // Codice Articolo (due campi distinti oppure potresti usare un oggetto embeddable)
    @Column(name = "codice_tipo")
    private String codiceTipo;  // Es. "EAN" o "SKU"
    @Column(name = "codice_valore")
    private String codiceValore; // Es. "1234567890123"

    // Unità di misura (es. "PZ", "KG", "NR" ecc.)
    @Column(name = "unita_misura")
    private String unitaMisura;

    // Natura (solo se il prodotto/servizio rientra in casistiche particolari: es. "N2", "N3", ecc.)
    @Column(name = "natura")
    private String natura;

    // Esigibilità IVA (es. "I" = immediata, "D" = differita...)
    @Column(name = "esigibilita_iva")
    private String esigibilitaIVA;
}
