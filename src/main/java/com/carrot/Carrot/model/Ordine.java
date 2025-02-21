package com.carrot.Carrot.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.carrot.Carrot.enumerator.CondizioniPagamento;
import com.carrot.Carrot.enumerator.TipoDocumento;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "ordini")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ordine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Associazione con Cliente
    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "data_ordine", nullable = false)
    private LocalDateTime dataOrdine = LocalDateTime.now();

    @Column(name = "numero_ordine", nullable = false)
    private String numeroOrdine;

    @Column(nullable = false)
    private BigDecimal totale;

    @Column(nullable = false)
    private boolean fatturato = false;

    @Column(nullable = false)
    private String stato;

    @OneToMany(mappedBy = "ordine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DettaglioOrdine> dettagliOrdine;

}
