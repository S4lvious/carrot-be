package com.carrot.Carrot.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "metodo_pagamento")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class MetodoPagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

}
