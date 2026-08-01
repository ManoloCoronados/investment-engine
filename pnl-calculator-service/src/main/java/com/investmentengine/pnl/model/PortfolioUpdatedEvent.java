package com.investmentengine.pnl.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;

public record PortfolioUpdatedEvent(


        Long userId,
        String symbol,
        String tradeId,
        BigDecimal newTotalQuantity,
        BigDecimal newAverageCost,
        BigDecimal realizedPnl,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant updatedAt


) {
}
