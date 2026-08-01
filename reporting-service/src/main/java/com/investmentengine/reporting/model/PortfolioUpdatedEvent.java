package com.investmentengine.reporting.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.Instant;

/**

 Evento que llega de portfolio-service
 cada vez que la posición de un usuario cambia
 */
public record PortfolioUpdatedEvent(
        Long userId,
        String symbol,
        String tradeId,
        BigDecimal newTotalQuantity,
        BigDecimal newAverageCost,
        BigDecimal realizedPnl,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant updatedAt
) {}