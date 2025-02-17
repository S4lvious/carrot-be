package com.carrot.Carrot.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name; // Mensile, Trimestrale, etc.

    @Column(nullable = false)
    private int durationDays; // Numero di giorni di validità

    @Column(nullable = false)
    private double price; // Prezzo in euro
    @Version
    private Integer version = 0;

    public Plan(UUID id, String name, int durationDays, double price, int version) {
        this.id = id;
        this.name = name;
        this.durationDays = durationDays;
        this.price = price;
        this.version = version;
    }

    public Plan(String name, int durationDays, double price) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.durationDays = durationDays;
        this.price = price;
        this.version = 0;
    }



}
