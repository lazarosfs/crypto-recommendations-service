package com.example.cryptorecommendationsservice.controller;

import com.example.cryptorecommendationsservice.dto.CryptoNormalizedRangeDTO;
import com.example.cryptorecommendationsservice.dto.CryptoStatsSimpleDTO;
import com.example.cryptorecommendationsservice.service.CryptoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/crypto")
public class CryptoController {

    private final CryptoService cryptoService;

    @Autowired
    public CryptoController(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    /**
     * Endpoint to retrieve all supported crypto symbols.
     *
     * @return List of supported crypto symbols.
     */
    @Operation(summary = "Get all supported crypto symbols", description = "Retrieves a list of all supported cryptocurrency symbols.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of supported cryptos retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No supported cryptos found")
    })
    @GetMapping("/supported")
    public List<String> getSupportedCryptos() {
        return cryptoService.getAllCryptoSymbols();
    }

    /**
     * Endpoint to retrieve stats (oldest, newest, min, max prices) for a specific crypto symbol.
     *
     * @param symbol The crypto symbol.
     * @return CryptoStatsSimpleDTO containing stats for the specified symbol.
     */
    @Operation(summary = "Get stats for a specific crypto", description = "Retrieves the oldest, newest, minimum, and maximum prices for a specified cryptocurrency symbol.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stats retrieved successfully for the specified crypto"),
            @ApiResponse(responseCode = "404", description = "Crypto not found")
    })
    @GetMapping("/{symbol}/stats")
    public CryptoStatsSimpleDTO getCryptoStats(@Parameter(description = "Symbol of the crypto to retrieve stats for") @PathVariable String symbol) {
        return cryptoService.getCryptoStats(symbol);
    }

    /**
     * Endpoint to retrieve a sorted list of cryptos by normalized range (descending).
     *
     * @return List of CryptoNormalizedRange sorted by normalized range.
     */
    @Operation(summary = "Get cryptos sorted by normalized range", description = "Returns a descending sorted list of cryptos based on their normalized range (max - min / min).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sorted list of cryptos by normalized range retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No crypto data found for the normalized range")
    })
    @GetMapping("/normalized-range")
    public List<CryptoNormalizedRangeDTO> getCryptosByNormalizedRange() {
        return cryptoService.getCryptosSortedByNormalizedRange();
    }

    /**
     * Endpoint to retrieve the crypto with the highest normalized range for a specific date.
     *
     * @param date The date for which to find the crypto with the highest normalized range.
     * @return CryptoNormalizedRange for the crypto with the highest normalized range on the specified date.
     */
    @Operation(summary = "Get crypto with the highest normalized range for a date", description = "Finds and returns the crypto with the highest normalized range on a specified date.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Crypto with highest normalized range found successfully"),
            @ApiResponse(responseCode = "404", description = "No crypto data found for the specified date")
    })
    @GetMapping("/highest-normalized-range")
    public CryptoNormalizedRangeDTO getHighestNormalizedRangeForDate(
            @Parameter(description = "Date for which to find the highest normalized range", example = "2023-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return cryptoService.getHighestNormalizedRangeForDate(date);
    }
}
