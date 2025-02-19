package com.carrot.Carrot.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoRiferimento {

    @Column(name = "id_documento")
    private String idDocumento;

    @Column(name = "data_documento")
    private LocalDate data;

    @Column(name = "codice_commessa_convenzione")
    private String codiceCommessaConvenzione;

    @Column(name = "codice_cup")
    private String codiceCUP;

    @Column(name = "codice_cig")
    private String codiceCIG;
}
