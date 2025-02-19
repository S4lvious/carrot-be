package com.carrot.Carrot.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DatiCassaPrevidenziale {

    @Column(name = "tipo_cassa")
    private String tipoCassa;  // Es. "TC06" (Cassa Geometri) ecc.

    @Column(name = "al_cassa")
    private String alCassa;    // Aliquota contributiva cassa, es. "4.00"

    @Column(name = "importo_contributo_cassa", precision = 10, scale = 2)
    private BigDecimal importoContributoCassa;

    @Column(name = "imponibile_cassa", precision = 10, scale = 2)
    private BigDecimal imponibileCassa;

    @Column(name = "aliquota_iva_cassa", precision = 5, scale = 2)
    private BigDecimal aliquotaIVACassa; // IVA sulla cassa, se applicabile

    @Column(name = "natura_cassa")
    private String natura; // Natura IVA (es. "N1" ... "N6")

    @Column(name = "ritenuta_cassa")
    private Boolean ritenuta; // "SI"/"NO"? Oppure si può tenere un boolean
}
