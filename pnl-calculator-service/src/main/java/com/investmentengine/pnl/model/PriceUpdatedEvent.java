package com.investmentengine.pnl.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;

public record PriceUpdatedEvent(
        String symbol,
        BigDecimal price,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant updatedAt
) {
}
