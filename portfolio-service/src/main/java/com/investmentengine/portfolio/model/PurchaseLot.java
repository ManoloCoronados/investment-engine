package com.investmentengine.portfolio.model;

import java.math.BigDecimal;
import java.time.Instant;

public record PurchaseLot(
        Long id,
        Long userId,
        String symbol,
        String tradeId,
        BigDecimal originalQuantity,
        BigDecimal remainingQuantity,  ///cantidd de acciones que quedan en un lote
        BigDecimal purchasePrice,
        Instant purchasedAt
) {}