package com.carrot.Carrot.model;
import java.time.LocalDateTime;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "agenda_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgendaEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "agenda_id", nullable = false)
    private Agenda agenda;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = true)
    private Task task;  // 📌 Se è un Task

    @ManyToOne
    @JoinColumn(name = "appuntamento_id", nullable = true)
    private Appuntamento appuntamento;  // 📌 Se è un Appuntamento

    @Column(nullable = false)
    private LocalDateTime data;
}
