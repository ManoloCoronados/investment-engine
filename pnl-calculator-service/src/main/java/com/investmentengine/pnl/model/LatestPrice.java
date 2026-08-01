package com.investmentengine.pnl.model;

import java.math.BigDecimal;

public record LatestPrice(
        String symbol,
        BigDecimal price
) {
}
