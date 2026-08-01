package com.investmentengine.reporting.model;

import java.math.BigDecimal;

/**
 *
 // Igual que PositionView, pero mostrando cuánto está ganando o perdiendo ahora */
public record PnlView(
        Long userId,
        String symbol,
        BigDecimal unrealizedPnl,
        BigDecimal currentPrice
) {}