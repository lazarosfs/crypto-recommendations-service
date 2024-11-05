package com.example.cryptorecommendationsservice.repository;

import com.example.cryptorecommendationsservice.model.Crypto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CryptoRepository extends JpaRepository<Crypto, Long> {
    Optional<Crypto> findBySymbol(String symbol);
}

