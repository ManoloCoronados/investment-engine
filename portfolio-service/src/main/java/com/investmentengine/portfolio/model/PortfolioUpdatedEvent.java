package com.investmentengine.portfolio.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.Instant;

public record PortfolioUpdatedEvent(
        Long userId,
        String symbol,
        String tradeId,
        BigDecimal newTotalQuantity,
        BigDecimal newAverageCost,
        BigDecimal realizedPnl,        // null si fue un BUY, tiene valor si fue SELL
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant updatedAt
) {}