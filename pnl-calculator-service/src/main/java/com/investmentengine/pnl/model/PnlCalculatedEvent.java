package com.investmentengine.pnl.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;

public record PnlCalculatedEvent(
        Long userId,
        String symbol,
        BigDecimal unrealizedPnl,
        BigDecimal currentPrice,
        BigDecimal totalQuantity,
        BigDecimal averageCost,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant calculatedAt) {
}
