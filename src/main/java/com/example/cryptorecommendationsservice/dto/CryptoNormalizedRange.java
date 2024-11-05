package com.example.cryptorecommendationsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CryptoNormalizedRange {
    private String symbol;
    private BigDecimal normalizedRange;
}

