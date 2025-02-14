package com.carrot.Carrot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "operazioni")
public class Operazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String entita;
    private String tipo;
    private String descrizione;
    private LocalDateTime dataOperazione;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    // **Costruttore Vuoto**
    public Operazione() {
    }

    // **Costruttore con Parametri**
    public Operazione(String entita, String tipo, String descrizione, LocalDateTime dataOperazione, User user) {
        this.entita = entita;
        this.tipo = tipo;
        this.descrizione = descrizione;
        this.dataOperazione = dataOperazione;
        this.user = user;
    }

    // **Getter e Setter**
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }



    public void setId(Long id) {
        this.id = id;
    }

    public String getEntita() {
        return entita;
    }

    public void setEntita(String entita) {
        this.entita = entita;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public LocalDateTime getDataOperazione() {
        return dataOperazione;
    }

    public void setDataOperazione(LocalDateTime dataOperazione) {
        this.dataOperazione = dataOperazione;
    }
}
