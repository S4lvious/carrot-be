package com.carrot.Carrot.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "appuntamenti")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Appuntamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User organizzatore;  // Chi ha creato l'evento

    @Column(nullable = false)
    private String titolo;

    @Column(nullable = true)
    private String descrizione;

    @Column(nullable = false)
    private LocalDateTime dataInizio;

    @Column(nullable = false)
    private LocalDateTime dataFine;

    @Column(nullable = false)
    private boolean notificheAttive = true;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = true)
    private Cliente cliente;

    @ManyToMany
    @JoinTable(
        name = "partecipanti_appuntamento",
        joinColumns = @JoinColumn(name = "appuntamento_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> partecipanti;
}
