package com.carrot.Carrot.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "prodotti")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Prodotto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Column(nullable = false)
    private String nome;

    private String descrizione;

    @Column(nullable = false)
    private BigDecimal prezzo;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(name = "aliquota_iva", nullable = false)
    private BigDecimal aliquotaIVA;

    @Column(nullable = false)
    private int quantita;

    @Column(nullable = false)
    private boolean esauribile;
}
