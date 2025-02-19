package com.carrot.Carrot.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.carrot.Carrot.enumerator.ModalitaPagamento;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DettaglioPagamento {

    @Column(name = "beneficiario")
    private String beneficiario;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalita_pagamento")
    private ModalitaPagamento modalitaPagamento = ModalitaPagamento.MP05; // Default: MP05 se IBAN presente

    @Column(name = "data_scadenza_pagamento")
    private LocalDate dataScadenzaPagamento;

    @Column(name = "iban")
    private String iban;

    @Column(name = "importo_pagamento", precision = 10, scale = 2)
    private BigDecimal importoPagamento;

    @Column(name = "istituto_finanziario")
    private String istitutoFinanziario;
}
