package com.example.cryptorecommendationsservice.repository;

import com.example.cryptorecommendationsservice.model.CryptoPrice;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CryptoPriceRepository extends JpaRepository<CryptoPrice, Long> {

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE crypto_price", nativeQuery = true)
    void truncateTable();

    @Query("SELECT cp.crypto.symbol AS symbol, " +
            "MIN(cp.price) AS minPrice, " +
            "MAX(cp.price) AS maxPrice, " +
            "(SELECT cp1.price FROM CryptoPrice cp1 WHERE cp1.crypto.symbol = :symbol ORDER BY cp1.timestamp ASC LIMIT 1) AS oldestPrice, " +
            "(SELECT cp2.price FROM CryptoPrice cp2 WHERE cp2.crypto.symbol = :symbol ORDER BY cp2.timestamp DESC LIMIT 1) AS newestPrice " +
            "FROM CryptoPrice cp " +
            "WHERE cp.crypto.symbol = :symbol " +
            "GROUP BY cp.crypto.symbol")
    Optional<Object[]> findStatsBySymbol(@Param("symbol") String symbol);

    @Query("SELECT cp.crypto.symbol AS symbol, " +
            "CASE WHEN MIN(cp.price) = 0 THEN 0 ELSE (MAX(cp.price) - MIN(cp.price)) / MIN(cp.price) END AS normalizedRange " +
            "FROM CryptoPrice cp " +
            "GROUP BY cp.crypto.symbol")
    List<Object[]> findNormalizedAllStats();

    @Query("SELECT cp.crypto.symbol AS symbol, " +
            "MIN(cp.price) AS minPrice, " +
            "MAX(cp.price) AS maxPrice, " +
            "CASE WHEN MIN(cp.price) = 0 THEN 0 ELSE (MAX(cp.price) - MIN(cp.price)) / MIN(cp.price) END AS normalizedRange " +
            "FROM CryptoPrice cp " +
            "WHERE cp.timestamp BETWEEN :startOfDay AND :endOfDay " +
            "GROUP BY cp.crypto.symbol")
    List<Object[]> findNormalizedRangeForDate(@Param("startOfDay") long startOfDay, @Param("endOfDay") long endOfDay);

    @Modifying
    @Transactional
    @Query(value = "MERGE INTO crypto_price AS target " +
            "USING (SELECT CAST(? AS BIGINT) AS timestamp, CAST(? AS DECIMAL(20, 8)) AS price, " +
            "(SELECT id FROM crypto WHERE symbol = ?) AS crypto_id) AS source " +
            "ON target.crypto_id = source.crypto_id AND target.timestamp = source.timestamp " +
            "WHEN MATCHED THEN UPDATE SET target.price = source.price " +
            "WHEN NOT MATCHED THEN INSERT (timestamp, price, crypto_id) VALUES (source.timestamp, source.price, source.crypto_id)",
            nativeQuery = true)
    void upsertCryptoPrice(long timestamp, BigDecimal price, String symbol);

}
