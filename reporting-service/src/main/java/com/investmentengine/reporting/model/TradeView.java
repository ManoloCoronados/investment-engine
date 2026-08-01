package com.investmentengine.reporting.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 *
  Una sola operación (compra o venta) que el usuario hizo en una fecha específica.
  Si el usuario hizo 50 trades, vas a tener 50 objetos TradeView distintos.
 */
public record TradeView(
        String tradeId,
        Long userId,
        String symbol,
        BigDecimal quantity,
        BigDecimal price,
        String tradeType,
        Instant executedAt
) {}