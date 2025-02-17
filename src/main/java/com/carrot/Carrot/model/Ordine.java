package com.carrot.Carrot.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "ordini")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Ordine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "data_ordine", nullable = false)
    private LocalDateTime dataOrdine = LocalDateTime.now();

    @Column(nullable = false)
    private BigDecimal totale;

    @Column(nullable = false)
    private boolean fatturato = false;

    @Column(nullable = false)
    private String stato;

    @OneToMany(mappedBy = "ordine", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<DettaglioOrdine> dettagliOrdine;

    @Transient
    public String getNomeCliente() {
        return cliente != null ? cliente.getNome() : "";
    }
}
