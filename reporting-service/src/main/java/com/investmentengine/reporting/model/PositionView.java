package com.investmentengine.reporting.model;

import java.math.BigDecimal;

/**

 // El estado ACTUAL de un símbolo en el portafolio del usuario.
 // Si el usuario tiene 3 símbolos distintos (AAPL, TSLA, GOOGL),
 // vas a tener exactamente 3 objetos PositionView — uno por símbolo, no uno por trade.

 */
public record PositionView(
        Long userId,
        String symbol,
        BigDecimal totalQuantity,
        BigDecimal averageCost
) {}