package com.carrot.Carrot.model;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "progetti")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Progetto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ordine_id", nullable = true)
    private Ordine ordine;  // 📌 Un Progetto appartiene a un Ordine

    @ManyToMany
    @JoinTable(
        name = "progetto_partecipanti",
        joinColumns = @JoinColumn(name = "progetto_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> partecipanti;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = true)
    private String descrizione;

    @Column(nullable = false)
    private LocalDateTime dataCreazione = LocalDateTime.now();

    @OneToMany(mappedBy = "progetto", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Task> tasks;
}
