package com.carrot.Carrot.model;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "progetto_id", nullable = false)
    private Progetto progetto;  // 📌 Un Task appartiene a un Progetto

    @ManyToMany
    @JoinTable(
        name = "task_assegnato_a",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> assegnatoA;

    @Column(nullable = false)
    private String titolo;

    @Column(nullable = true)
    private String descrizione;

    @Column(nullable = false)
    private LocalDateTime dataCreazione = LocalDateTime.now();

    @Column(nullable = true)
    private LocalDateTime dataScadenza;

    @Enumerated(EnumType.STRING)
    private StatoTask stato = StatoTask.TODO;

    public enum StatoTask {
        TODO, IN_PROGRESS, COMPLETATO
    }
}
