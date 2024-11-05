package com.example.cryptorecommendationsservice.service;

import com.example.cryptorecommendationsservice.dto.CryptoNormalizedRange;
import com.example.cryptorecommendationsservice.dto.CryptoStatsDTO;
import com.example.cryptorecommendationsservice.dto.CryptoStatsSimpleDTO;
import com.example.cryptorecommendationsservice.exception.ResourceNotFoundException;
import com.example.cryptorecommendationsservice.model.Crypto;
import com.example.cryptorecommendationsservice.repository.CryptoPriceRepository;
import com.example.cryptorecommendationsservice.repository.CryptoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CryptoService {

    private static final Logger logger = LoggerFactory.getLogger(CryptoService.class);

    private final CryptoRepository cryptoRepository;
    private final CryptoPriceRepository cryptoPriceRepository;

    @Autowired
    public CryptoService(CryptoRepository cryptoRepository, CryptoPriceRepository cryptoPriceRepository) {
        this.cryptoRepository = cryptoRepository;
        this.cryptoPriceRepository = cryptoPriceRepository;
    }

    /**
     * Fetches all available crypto symbols from the repository.
     *
     * @return List of all crypto symbols.
     */
    @Operation(summary = "Fetch all supported crypto symbols")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of all supported crypto symbols"),
            @ApiResponse(responseCode = "404", description = "No crypto symbols found")
    })
    public List<String> getAllCryptoSymbols() {
        logger.info("Fetching all supported crypto symbols");
        List<String> symbols = cryptoRepository.findAll().stream()
                .map(Crypto::getSymbol)
                .collect(Collectors.toList());
        logger.info("Found {} supported cryptos", symbols.size());
        return symbols;
    }

    /**
     * Fetches the oldest, newest, minimum, and maximum prices for a specific crypto symbol.
     *
     * @param symbol The crypto symbol.
     * @return CryptoStatsDTO containing stats for the specified symbol.
     */
    @Operation(summary = "Get stats for a specific crypto symbol")
    public CryptoStatsSimpleDTO getCryptoStats(String symbol) {
        logger.info("Fetching stats for crypto {}", symbol);

        // Fetching the result which is expected to be an Object[] containing one element: an array of stats
        Object[] resultArray = cryptoPriceRepository.findStatsBySymbol(symbol)
                .orElseThrow(() -> {
                    logger.error("No price data found for crypto {}", symbol);
                    return new ResourceNotFoundException("No price data found for crypto: " + symbol);
                });

        // The actual data we need is in resultArray[0], which is an Object[] containing the details
        Object[] result = (Object[]) resultArray[0];

        logger.debug("Retrieved result for {}: {}", symbol, Arrays.toString(result));

        // Ensure the result has the expected length
        if (result.length < 5) { // Expecting symbol, minPrice, maxPrice, oldestPrice, newestPrice
            logger.error("Unexpected result length for crypto stats: {}", result.length);
            throw new ResourceNotFoundException("Unexpected result format for crypto stats");
        }

        try {
            String fetchedSymbol = (String) result[0];  // Assuming index 0 is symbol
            BigDecimal minPrice = (BigDecimal) result[1]; // Assuming index 1 is minPrice
            BigDecimal maxPrice = (BigDecimal) result[2]; // Assuming index 2 is maxPrice
            BigDecimal oldestPrice = (BigDecimal) result[3]; // Assuming index 3 is oldestPrice
            BigDecimal newestPrice = (BigDecimal) result[4]; // Assuming index 4 is newestPrice

            logger.debug("Stats for {}: oldest={}, newest={}, min={}, max={}", fetchedSymbol, oldestPrice, newestPrice, minPrice, maxPrice);

            // Return the DTO with normalizedRange set to null
            return new CryptoStatsSimpleDTO(fetchedSymbol, oldestPrice, newestPrice, minPrice, maxPrice);
        } catch (ArrayIndexOutOfBoundsException | ClassCastException e) {
            logger.error("Data format error for crypto stats: {}", e.getMessage(), e);
            throw new ResourceNotFoundException("Data format error for crypto stats");
        }
    }

    /**
     * Fetches aggregated stats (min, max, oldest, newest prices) for all cryptos.
     *
     * @return List of CryptoStatsDTO for all cryptos.
     */
    @Operation(summary = "Get aggregated stats for all cryptos")
    public List<CryptoStatsDTO> getAllCryptoStats() {
        logger.info("Fetching aggregated stats for all cryptos");

        List<Object[]> results = cryptoPriceRepository.findAllStats();

        if (results.isEmpty()) {
            logger.error("No price data found for any crypto");
            throw new ResourceNotFoundException("No crypto data found.");
        }

        // Map each result row to a CryptoStatsDTO
        return results.stream()
                .map(this::getCryptoStatsDTO)
                .sorted(Comparator.comparing(CryptoStatsDTO::getNormalizedRange).reversed()) // Sort by normalized range in descending order
                .collect(Collectors.toList());
    }

    /**
     * Converts an array of database query results into a CryptoStatsDTO.
     *
     * @param result An array of objects representing the query result, expected to contain:
     *               - [0] String symbol
     *               - [1] BigDecimal minPrice
     *               - [2] BigDecimal maxPrice
     *               - [3] BigDecimal oldestPrice
     *               - [4] BigDecimal newestPrice
     *               - [5] BigDecimal normalizedRange
     * @return A CryptoStatsDTO object populated with the data from the result array.
     * @throws ClassCastException if the data types in the result array do not match the expected types.
     * @throws ResourceNotFoundException if there is a data format error when processing the result.
     */
    private CryptoStatsDTO getCryptoStatsDTO(Object[] result) {
        try {
            String symbol = (String) result[0];
            BigDecimal minPrice = (BigDecimal) result[1];
            BigDecimal maxPrice = (BigDecimal) result[2];
            BigDecimal oldestPrice = (BigDecimal) result[3];
            BigDecimal newestPrice = (BigDecimal) result[4];
            BigDecimal normalizedRange = (BigDecimal) result[5];

            logger.debug("Stats for {}: oldest={}, newest={}, min={}, max={}, normalizedRange={}",
                    symbol, oldestPrice, newestPrice, minPrice, maxPrice, normalizedRange);
            return new CryptoStatsDTO(symbol, oldestPrice, newestPrice, minPrice, maxPrice, normalizedRange);
        } catch (ClassCastException e) {
            logger.error("Error casting result for crypto stats: {}", e.getMessage());
            throw new ResourceNotFoundException("Data format error for crypto stats");
        }
    }

    /**
     * Calculates and returns a sorted list of cryptos based on their normalized range, in descending order.
     *
     * @return List of CryptoNormalizedRange sorted by normalized range.
     */
    @Operation(summary = "Get all cryptos sorted by normalized range (max - min / min)")
    public List<CryptoNormalizedRange> getCryptosSortedByNormalizedRange() {
        logger.info("Calculating normalized range for all cryptos");

        List<CryptoStatsDTO> statsList = getAllCryptoStats();

        return statsList.stream()
                .map(stats -> {
                    BigDecimal minPrice = stats.getMinPrice();
                    BigDecimal maxPrice = stats.getMaxPrice();

                    // Calculate normalized range: (max - min) / min
                    BigDecimal normalizedRange = (minPrice.compareTo(BigDecimal.ZERO) > 0) ?
                            maxPrice.subtract(minPrice).divide(minPrice, 8, RoundingMode.HALF_UP) :
                            BigDecimal.ZERO;

                    logger.debug("Normalized range for {}: {}", stats.getSymbol(), normalizedRange);
                    return new CryptoNormalizedRange(stats.getSymbol(), normalizedRange);
                })
                .sorted(Comparator.comparing(CryptoNormalizedRange::getNormalizedRange).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Fetches the crypto with the highest normalized range for a specific date.
     *
     * @param date The date for which to calculate the highest normalized range.
     * @return CryptoNormalizedRange for the crypto with the highest normalized range on the specified date.
     */
    @Operation(summary = "Get the crypto with the highest normalized range for a specific date")
    public CryptoNormalizedRange getHighestNormalizedRangeForDate(LocalDate date) {
        logger.info("Fetching crypto with the highest normalized range for date: {}", date);

        // Calculate the start and end of the specified date in epoch milliseconds
        long startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endOfDay = date.atTime(23, 59, 59, 999_999_999).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        // Pass the calculated timestamps to the repository method
        List<Object[]> results = cryptoPriceRepository.findNormalizedRangeForDate(startOfDay, endOfDay);

        if (results.isEmpty()) {
            logger.error("No price data found for the given date: {}", date);
            throw new ResourceNotFoundException("No crypto data found for the given date.");
        }

        return results.stream()
                .map(result -> {
                    String symbol = (String) result[0];
                    BigDecimal minPrice = (BigDecimal) result[1];
                    BigDecimal maxPrice = (BigDecimal) result[2];

                    // Calculate the normalized range: (max - min) / min, handling any cases where minPrice is 0
                    BigDecimal normalizedRange = BigDecimal.ZERO;
                    if (minPrice.compareTo(BigDecimal.ZERO) > 0) {
                        normalizedRange = maxPrice.subtract(minPrice).divide(minPrice, 8, RoundingMode.HALF_UP);
                    }

                    return new CryptoNormalizedRange(symbol, normalizedRange);
                })
                .max(Comparator.comparing(CryptoNormalizedRange::getNormalizedRange))
                .orElseThrow(() -> new ResourceNotFoundException("No crypto data with a valid normalized range found for the given date."));
    }

}
