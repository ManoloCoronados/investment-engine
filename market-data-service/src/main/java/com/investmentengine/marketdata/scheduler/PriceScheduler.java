package com.investmentengine.marketdata.scheduler;


import com.investmentengine.marketdata.producer.PriceUpdatedProducer;
import com.investmentengine.marketdata.repository.MarketPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Component
@Slf4j
public class PriceScheduler {

    private final MarketPriceRepository repository;
    private final PriceUpdatedProducer producer;
    private final Random random = new Random();


    /**
     * creamos coleccioin de precios base que seran volatios
     */
    private static final Map<String, BigDecimal> BASE_PRICES = Map.of(
            "AAPL", new BigDecimal("175.00"),
            "TSLA", new BigDecimal("245.00"),
            "GOOGL", new BigDecimal("140.00"),
            "MSFT", new BigDecimal("415.00"),
            "AMZN", new BigDecimal("185.00"),
            "NVDA", new BigDecimal("875.00"),
            "META", new BigDecimal("505.00"),
            "NFLX", new BigDecimal("625.00")
    );


    private static final List<String> SYMBOLS = List.copyOf(BASE_PRICES.keySet());

    private final Map<String, BigDecimal> currentPrices = new ConcurrentHashMap<>(BASE_PRICES);

    /**
     * metodo que se ejecutara cada 3 egundos
     */
    @Scheduled(fixedDelayString = "${scheduler.price.interval-ms:3000}")
    public void updatePrice() {

        String symbol = SYMBOLS.get(random.nextInt(SYMBOLS.size()));
        BigDecimal currentPrice = currentPrices.get(symbol);

        BigDecimal variation = currentPrice.multiply(
                BigDecimal.valueOf((random.nextDouble() * 3) - 1.5)
                        .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
        );
        BigDecimal newPrice = currentPrice.add(variation).setScale(2, RoundingMode.HALF_UP);

        currentPrices.put(symbol, newPrice);

        Instant now = Instant.now();

        repository.insert(symbol, newPrice, now);
        producer.publish(symbol, newPrice, now);

        log.debug("Precio actualizado -> symbol={} price={}", symbol, newPrice);
    }
}