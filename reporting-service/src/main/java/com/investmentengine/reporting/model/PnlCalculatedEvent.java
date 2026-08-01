package com.investmentengine.reporting.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.Instant;


/**
 *
 // Evento que llega de pnl-calculator-service
    con el P&L no realizado ya calculado
 */
public record PnlCalculatedEvent(
        Long userId,
        String symbol,
        BigDecimal unrealizedPnl,
        BigDecimal currentPrice,
        BigDecimal totalQuantity,
        BigDecimal averageCost,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant calculatedAt
) {}