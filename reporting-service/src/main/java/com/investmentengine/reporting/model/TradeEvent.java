package com.investmentengine.reporting.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.Instant;

/**
 *
 // Este record representa el EVENTO que llega de Kafka, no lo que ve el frontend.
 // Tiene que coincidir exactamente con lo que publica trade-ingestion-service.
 */
public record TradeEvent(
        String tradeId,
        Long userId,
        String symbol,
        BigDecimal quantity,
        BigDecimal price,
        TradeType tradeType,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant executedAt
) {
    public enum TradeType {
        BUY, SELL
    }
}