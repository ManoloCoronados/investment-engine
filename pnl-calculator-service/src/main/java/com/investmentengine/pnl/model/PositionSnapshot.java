package com.investmentengine.pnl.model;

import java.math.BigDecimal;

public record PositionSnapshot(
        Long userId,
        String symbol,
        BigDecimal totalQuantity,
        BigDecimal averageCost


) {
}
