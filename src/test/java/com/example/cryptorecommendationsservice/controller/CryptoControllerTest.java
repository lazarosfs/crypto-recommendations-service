package com.example.cryptorecommendationsservice.controller;

import com.example.cryptorecommendationsservice.repository.CryptoPriceRepository;
import com.example.cryptorecommendationsservice.repository.CryptoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CryptoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CryptoRepository cryptoRepository;

    @Autowired
    private CryptoPriceRepository cryptoPriceRepository;

    @Test
    @Operation(summary = "Get supported cryptocurrencies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of supported cryptocurrencies"),
            @ApiResponse(responseCode = "404", description = "No supported cryptocurrencies found")
    })
    public void testGetSupportedCryptos() throws Exception {
        mockMvc.perform(get("/api/crypto/supported")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$", contains("BTC", "DOGE", "ETH", "LTC", "XRP")));
    }

    @Test
    @Operation(summary = "Get stats for a specific cryptocurrency")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the stats for the specified cryptocurrency"),
            @ApiResponse(responseCode = "404", description = "No price data found for the specified cryptocurrency")
    })
    public void testGetCryptoStats() throws Exception {
        mockMvc.perform(get("/api/crypto/BTC/stats")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.symbol", is("BTC")))
                .andExpect(jsonPath("$.oldestPrice", is(46813.21)))
                .andExpect(jsonPath("$.newestPrice", is(38415.79)))
                .andExpect(jsonPath("$.minPrice", is(33276.59)))
                .andExpect(jsonPath("$.maxPrice", is(47722.66)));
    }

    @Test
    @Operation(summary = "Get the cryptocurrency with the highest normalized range for a specific date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the crypto with the highest normalized range for the specified date"),
            @ApiResponse(responseCode = "404", description = "No price data found for the specified date")
    })
    public void testGetHighestNormalizedRangeForDate() throws Exception {
        mockMvc.perform(get("/api/crypto/highest-normalized-range?date=2022-01-24")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.symbol", is("XRP")))
                .andExpect(jsonPath("$.normalizedRange", is(0.09009972)));
    }

    @Test
    @Operation(summary = "Get normalized range for all cryptocurrencies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the normalized range for all cryptocurrencies"),
            @ApiResponse(responseCode = "404", description = "No price data found for any cryptocurrency")
    })
    public void testGetNormalizedRange() throws Exception {
        mockMvc.perform(get("/api/crypto/normalized-range")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].symbol", is("ETH")))
                .andExpect(jsonPath("$[1].symbol", is("XRP")))
                .andExpect(jsonPath("$[2].symbol", is("DOGE")))
                .andExpect(jsonPath("$[3].symbol", is("LTC")))
                .andExpect(jsonPath("$[4].symbol", is("BTC")));
    }
}
