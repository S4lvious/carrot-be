package com.carrot.Carrot.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Plan {

    @JdbcTypeCode(SqlTypes.BINARY) // CORRETTO PER HIBERNATE 6+
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private int durationDays;

    @Column(nullable = false)
    private double price;

    public Plan(String name, int durationDays, double price) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.durationDays = durationDays;
        this.price = price;
    }
}
