package com.carrot.Carrot.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DatiBollo {

    // "SI"/"NO" nel JSON; qui spesso si usa un boolean
    // Se preferisci memorizzare proprio il valore "SI"/"NO", puoi usare un String
    @Column(name = "bollo_virtuale")
    private Boolean bolloVirtuale;

    @Column(name = "importo_bollo", precision = 10, scale = 2)
    private BigDecimal importoBollo;
}
