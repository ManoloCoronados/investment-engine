package com.investmentengine.pnl.repository;

import com.investmentengine.pnl.model.LatestPrice;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LatestPriceRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<LatestPrice> MAPPER = (rs, rowNum) ->
            new LatestPrice(
                    rs.getString("symbol"),
                    rs.getBigDecimal("price")
            );

    // Busca el último precio conocido de un símbolo —
    // se usa cuando llega un PortfolioUpdatedEvent y necesitas saber
    // a cuánto vale ahora ese símbolo para calcular el P&L
    public Optional<LatestPrice> findBySymbol(String symbol) {
        String sql = "SELECT * FROM latest_prices WHERE symbol = ?";
        List<LatestPrice> results = jdbc.query(sql, MAPPER, symbol);
        return results.stream().findFirst();
    }

    // Mantiene esta tabla sincronizada con lo que publica market-data-service
    public void upsert(String symbol, BigDecimal price, Instant updatedAt) {
        String sql = """
            INSERT INTO latest_prices (symbol, price, updated_at)
            VALUES (?, ?, ?)
            ON CONFLICT (symbol)
            DO UPDATE SET
                price = EXCLUDED.price,
                updated_at = EXCLUDED.updated_at
            """;
        jdbc.update(sql, symbol, price, java.sql.Timestamp.from(updatedAt));
    }
}