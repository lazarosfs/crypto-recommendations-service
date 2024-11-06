package com.example.cryptorecommendationsservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "crypto_price", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"timestamp", "crypto_id"})
})
public class CryptoPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long timestamp;

    @Column(precision = 20, scale = 8)
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "crypto_id")
    private Crypto crypto;

    // Getters and setters omitted for brevity
}

