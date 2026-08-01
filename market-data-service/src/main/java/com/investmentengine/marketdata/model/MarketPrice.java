package com.investmentengine.marketdata.model;

import java.math.BigDecimal;
import java.time.Instant;

public record MarketPrice(
        Long id,
        String symbol,
        BigDecimal price,
        Instant recordedAt
) {}