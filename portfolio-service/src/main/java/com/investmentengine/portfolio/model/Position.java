

package com.investmentengine.portfolio.model;

import java.math.BigDecimal;

public record Position(
        Long userId,
        String symbol,
        BigDecimal totalQuantity,
        BigDecimal averageCost
) {}