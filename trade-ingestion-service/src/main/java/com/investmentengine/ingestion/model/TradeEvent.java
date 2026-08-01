package com.investmentengine.ingestion.model;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;

public record TradeEvent(

        String tradeId,
        Long userId,
        String symbol,
        BigDecimal quantity,
        BigDecimal price,
        TradeType tradeType,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant executedAt


) {
    public enum TradeType {
        BUY, SELL
    }

}
