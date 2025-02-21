package com.carrot.Carrot.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "documenti")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;      // Nome del file
    private String percorso;  // Percorso su Google Cloud Storage

    @ManyToOne
    @JoinColumn(name = "ordine_id", nullable = false)
    private Ordine ordine;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
