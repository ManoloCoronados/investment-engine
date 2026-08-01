package com.investmentengine.ingestion.model;

import java.math.BigDecimal;
import java.time.Instant;

public record TradeRecord(

        Long id,
        String tradeId,
        Long userId,
        String symbol,
        BigDecimal quantity,
        BigDecimal price,
        TradeEvent.TradeType tradeType,
        Instant executedAt
) {
}
