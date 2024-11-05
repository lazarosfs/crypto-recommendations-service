package com.example.cryptorecommendationsservice.repository;

import com.example.cryptorecommendationsservice.model.Crypto;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CryptoRepository extends JpaRepository<Crypto, Long> {
    Optional<Crypto> findBySymbol(String symbol);

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE crypto", nativeQuery = true)
    void truncateTable();

}

