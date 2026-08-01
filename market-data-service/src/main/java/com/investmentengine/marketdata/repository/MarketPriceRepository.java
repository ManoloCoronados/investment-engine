package com.investmentengine.marketdata.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;

@RequiredArgsConstructor
@Repository
public class MarketPriceRepository {

    private final JdbcTemplate jdbc;

    public void insert(String symbol, BigDecimal price, Instant recordedAt) {
        String sql = """
            INSERT INTO market_prices (symbol, price, recorded_at)
            VALUES (?, ?, ?)
            """;
        jdbc.update(sql, symbol, price, java.sql.Timestamp.from(recordedAt));
    }
}


