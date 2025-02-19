package com.carrot.Carrot.model;

import jakarta.persistence.*;
import lombok.*;

import com.carrot.Carrot.enumerator.CondizioniPagamento;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DatiPagamento {

    @Enumerated(EnumType.STRING)
    @Column(name = "condizioni_pagamento")
    private CondizioniPagamento condizioniPagamento; // TP01, TP02, TP03...

    @Embedded
    private DettaglioPagamento dettaglioPagamento;
}
