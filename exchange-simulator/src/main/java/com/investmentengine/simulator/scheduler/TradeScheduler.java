package com.investmentengine.simulator.scheduler;

import com.investmentengine.simulator.model.TradeEvent;
import com.investmentengine.simulator.model.TradeEvent.TradeType;
import com.investmentengine.simulator.producer.TradeEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor

public class TradeScheduler {

    private final TradeEventProducer producer;
    private final Random random = new Random();

    private static final List<Long> USER_IDS = List.of(1L, 2L, 3L, 4L, 5L);

    private static final Map<String, BigDecimal> BASE_PRICES = Map.of(
            "AAPL",  new BigDecimal("175.00"),
            "TSLA",  new BigDecimal("245.00"),
            "GOOGL", new BigDecimal("140.00"),
            "MSFT",  new BigDecimal("415.00"),
            "AMZN",  new BigDecimal("185.00"),
            "NVDA",  new BigDecimal("875.00"),
            "META",  new BigDecimal("505.00"),
            "NFLX",  new BigDecimal("625.00")
    );

    private static final List<String> SYMBOLS = List.copyOf(BASE_PRICES.keySet());

    private final Map<String, BigDecimal> currentPrices = new ConcurrentHashMap<>(BASE_PRICES);

    // userId -> symbol -> quantity owned
    // Esto es el inventario interno del simulador
    private final Map<Long, Map<String, BigDecimal>> portfolio = new ConcurrentHashMap<>();

    @Scheduled(fixedDelayString = "${simulator.trade.interval-ms:2000}")
    public void generateTrade() {

        Long userId   = USER_IDS.get(random.nextInt(USER_IDS.size()));
        String symbol = SYMBOLS.get(random.nextInt(SYMBOLS.size()));

        BigDecimal currentPrice = currentPrices.get(symbol);
        BigDecimal variation = currentPrice.multiply(
                BigDecimal.valueOf((random.nextDouble() * 4) - 2)
                        .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
        );
        BigDecimal newPrice = currentPrice.add(variation)
                .setScale(2, RoundingMode.HALF_UP);
        currentPrices.put(symbol, newPrice);

        // Inventario del usuario para este símbolo
        Map<String, BigDecimal> userPortfolio = portfolio
                .computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        BigDecimal owned = userPortfolio.getOrDefault(symbol, BigDecimal.ZERO);

        TradeType tradeType;
        BigDecimal quantity;

        if (owned.compareTo(BigDecimal.ZERO) <= 0) {
            // Sin posición — solo puede comprar
            tradeType = TradeType.BUY;
            quantity = BigDecimal.valueOf(1 + random.nextInt(100))
                    .setScale(4, RoundingMode.HALF_UP);
        } else if (random.nextDouble() < 0.6) {
            // Tiene posición y toca BUY
            tradeType = TradeType.BUY;
            quantity = BigDecimal.valueOf(1 + random.nextInt(100))
                    .setScale(4, RoundingMode.HALF_UP);
        } else {
            // SELL — máximo lo que tiene, mínimo 1
            tradeType = TradeType.SELL;
            double maxSell = owned.doubleValue();
            quantity = BigDecimal.valueOf(1 + random.nextInt((int) Math.max(1, maxSell)))
                    .min(owned)
                    .setScale(4, RoundingMode.HALF_UP);
        }

        // Actualiza inventario interno
        if (tradeType == TradeType.BUY) {
            userPortfolio.put(symbol, owned.add(quantity));
        } else {
            userPortfolio.put(symbol, owned.subtract(quantity));
        }

        TradeEvent event = TradeEvent.random(userId, symbol, newPrice, quantity, tradeType);
        producer.publish(event);

        log.debug("Trade generado -> userId={} symbol={} type={} price={} qty={} owned={}",
                userId, symbol, tradeType, newPrice, quantity,
                userPortfolio.get(symbol));
    }
}