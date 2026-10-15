package com.example.ecommercecheckout.idempotency;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"scope", "idempotencyKey"}))
@Getter
@Setter
@NoArgsConstructor
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private IdempotencyScope scope;

    private String idempotencyKey;

    private Long fingerprint;

    private Long orderId;

    private Instant createdAt = Instant.now();

    public IdempotencyRecord(IdempotencyScope scope, String idempotencyKey, Long fingerprint, Long orderId) {
        this.scope = scope;
        this.idempotencyKey = idempotencyKey;
        this.fingerprint = fingerprint;
        this.orderId = orderId;
    }
}
