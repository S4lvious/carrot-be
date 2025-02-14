package com.carrot.Carrot.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.carrot.Carrot.model.Ordine;

public class fatturarequest {

    private com.carrot.Carrot.model.Ordine ordine;
    private boolean applicareRitenuta;
    private BigDecimal ritenutaAcconto;
    private LocalDate scadenza;
    private String stato;

    // Costruttore vuoto (necessario per la deserializzazione)
    public fatturarequest() {
    }

    // Getters e Setters
    public Ordine getOrdine() {
        return ordine;
    }

    public void setStato (String stato) {
        this.stato = stato;
    }

    public String getStato () {
        return stato;
    }

    public void setOrdine(Ordine ordine) {
        this.ordine = ordine;
    }

    public boolean isApplicareRitenuta() {
        return applicareRitenuta;
    }

    public void setApplicareRitenuta(boolean applicareRitenuta) {
        this.applicareRitenuta = applicareRitenuta;
    }

    public BigDecimal getRitenutaAcconto() {
        return ritenutaAcconto;
    }

    public void setRitenutaAcconto(BigDecimal ritenutaAcconto) {
        this.ritenutaAcconto = ritenutaAcconto;
    }

    public LocalDate getScadenza() {
        return scadenza;
    }

    public void setScadenza(LocalDate scadenza) {
        this.scadenza = scadenza;
    }

}