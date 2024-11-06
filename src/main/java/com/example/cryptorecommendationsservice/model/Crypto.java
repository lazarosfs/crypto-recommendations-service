package com.example.cryptorecommendationsservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
public class Crypto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;

    @OneToMany(mappedBy = "crypto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CryptoPrice> prices;
}

