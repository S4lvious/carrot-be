package com.carrot.Carrot.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.carrot.Carrot.enumerator.TipoDocumento;
// Importa i tuoi embeddable se vuoi
import com.carrot.Carrot.model.DatiBollo;
import com.carrot.Carrot.model.DatiCassaPrevidenziale;
import com.carrot.Carrot.model.DatiPagamento;
import com.carrot.Carrot.model.DocumentoRiferimento;
import com.carrot.Carrot.model.Ordine;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FatturaCompletaDTO {

    // Riferimento all'Ordine
    private Ordine ordine;

    // Parametri base (esistenti)
    private boolean inserisciMovimento;
    private boolean applicareRitenuta;
    private BigDecimal ritenutaAcconto;
    private LocalDate scadenza;
    private String stato;

    // Nuovi campi
    private TipoDocumento tipoDocumento;      // "FATT" o "NDC"
    private String causale;
    private String causalePagamento;

    // Embeddable
    private DatiBollo datiBollo;
    private DatiCassaPrevidenziale datiCassaPrevidenziale;
    private DatiPagamento datiPagamento;

    // Documenti di riferimento
    private DocumentoRiferimento datiOrdineAcquisto;
    private DocumentoRiferimento datiContratto;
    private DocumentoRiferimento datiConvenzione;
    private DocumentoRiferimento datiRicezione;
    private DocumentoRiferimento datiFattureCollegate;

    // Altri campi facoltativi a tua scelta...
}
