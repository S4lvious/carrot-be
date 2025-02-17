package com.carrot.Carrot.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "pending_subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PendingSubscription {

    @Id
    @JdbcTypeCode(SqlTypes.BINARY) // CORRETTO PER HIBERNATE 6+
    @Column(columnDefinition = "BINARY(16)")

    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;
}
