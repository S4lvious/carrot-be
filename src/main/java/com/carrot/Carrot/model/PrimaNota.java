package com.carrot.Carrot.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.carrot.Carrot.enumerator.TipoMovimento;

@Entity
@Table(name = "prima_nota")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class PrimaNota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dataOperazione;

    @Column(nullable = false)
    private String nome;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)    
    private CategoriaMovimento categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; 

    @ManyToOne
    @JoinColumn(name = "metodo_pagamento_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private MetodoPagamento metodoPagamento; 

    @Column(nullable = false)
    private BigDecimal importo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimento tipoMovimento;  // ENTRATA o USCITA

    @ManyToOne
    @JoinColumn(name = "fattura_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Fattura fattura;  // Se è un'entrata legata a una fattura emessa
    
    @Column(name = "incarico_id", nullable = true)
    private Long incaricoId;  // Se l’operazione

    private String bankTransactionId; 

}
