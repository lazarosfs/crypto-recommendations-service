package com.example.cryptorecommendationsservice.service;

import com.example.cryptorecommendationsservice.model.Crypto;
import com.example.cryptorecommendationsservice.model.CryptoPrice;
import com.example.cryptorecommendationsservice.repository.CryptoPriceRepository;
import com.example.cryptorecommendationsservice.repository.CryptoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;

/**
 * Service for importing cryptocurrency data from CSV files.
 */
@Service
public class CsvImportService {

    private static final Logger logger = LoggerFactory.getLogger(CsvImportService.class);

    private final CryptoRepository cryptoRepository;
    private final CryptoPriceRepository cryptoPriceRepository;

    @Autowired
    public CsvImportService(CryptoRepository cryptoRepository, CryptoPriceRepository cryptoPriceRepository) {
        this.cryptoRepository = cryptoRepository;
        this.cryptoPriceRepository = cryptoPriceRepository;
    }

    /**
     * Imports cryptocurrency data from the provided InputStream.
     *
     * @param inputStream The InputStream containing CSV data.
     * @throws RuntimeException if an error occurs while processing the CSV file.
     */
    public void importCryptoData(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip header line if present
                if (line.trim().toLowerCase().startsWith("timestamp")) {
                    continue;
                }

                // Call parseAndSaveLine to process and save each CSV line
                parseAndSaveLine(line);
            }
        } catch (Exception e) {
            logger.error("Error processing CSV file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process CSV file", e); // Rethrow as a runtime exception
        }
    }

    /**
     * Parses a line from the CSV file and saves the corresponding cryptocurrency data.
     *
     * @param line The line from the CSV file.
     * @throws IllegalArgumentException if the line does not contain the required number of columns or if parsing fails.
     */
    private void parseAndSaveLine(String line) {
        String[] columns = line.split(",");

        // Validate column count
        if (columns.length < 3) {
            throw new IllegalArgumentException("Line does not contain the required number of columns (3)");
        }

        try {
            long timestamp = parseLong(columns[0].trim());
            String symbol = parseString(columns[1].trim());
            BigDecimal price = parseBigDecimal(columns[2].trim());

            // Check if crypto already exists; if not, create a new one
            Crypto crypto = cryptoRepository.findBySymbol(symbol)
                    .orElseGet(() -> {
                        Crypto newCrypto = new Crypto();
                        newCrypto.setSymbol(symbol);
                        logger.info("Adding new crypto symbol to database: {}", symbol);
                        return cryptoRepository.save(newCrypto);
                    });

            // Save the crypto price
            CryptoPrice cryptoPrice = new CryptoPrice();
            cryptoPrice.setTimestamp(timestamp);
            cryptoPrice.setPrice(price);
            cryptoPrice.setCrypto(crypto);

            cryptoPriceRepository.save(cryptoPrice);
            logger.info("Saved price data for crypto symbol {} at timestamp {} with price {}", symbol, timestamp, price);

        } catch (IllegalArgumentException e) {
            logger.error("Error processing line: {}", line, e);
            throw e; // Rethrow to indicate a parsing error
        } catch (Exception e) {
            logger.error("Unexpected error while processing line: {}", line, e);
            throw new RuntimeException("Failed to process line: " + line, e);
        }
    }

    /**
     * Parses a String value into a long.
     *
     * @param value The string value to parse.
     * @return The parsed long value.
     * @throws IllegalArgumentException if the value cannot be parsed as a long.
     */
    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid timestamp format: " + value, e);
        }
    }

    /**
     * Parses a String value into a BigDecimal.
     *
     * @param value The string value to parse.
     * @return The parsed BigDecimal value.
     * @throws IllegalArgumentException if the value cannot be parsed as a BigDecimal.
     */
    private BigDecimal parseBigDecimal(String value) {
        try {
            return new BigDecimal(value).setScale(8, RoundingMode.HALF_UP); // Setting scale to 8 for precision
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid price format: " + value, e);
        }
    }

    /**
     * Validates and returns a String value.
     *
     * @param value The string value to validate.
     * @return The validated string value.
     * @throws IllegalArgumentException if the value is null or empty.
     */
    private String parseString(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Invalid symbol format: value is missing");
        }
        return value;
    }
}
