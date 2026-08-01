package com.investmentengine.reporting.repository;

import com.investmentengine.reporting.model.TradeEvent;
import com.investmentengine.reporting.model.TradeView;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TradeViewRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<TradeView> MAPPER = (rs, rowNum) ->
            new TradeView(
                    rs.getString("id"),
                    rs.getLong("user_id"),
                    rs.getString("symbol"),
                    rs.getBigDecimal("quantity"),
                    rs.getBigDecimal("price"),
                    rs.getString("trade_type"),
                    rs.getTimestamp("executed_at").toInstant()
            );

    public void insert(TradeEvent event) {
        String sql = """
                INSERT INTO trades_view (id, user_id, symbol, quantity, price, trade_type, executed_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO NOTHING
                """;
        jdbc.update(sql,
                UUID.fromString(event.tradeId()),
                event.userId(),
                event.symbol(),
                event.quantity(),
                event.price(),
                event.tradeType().name(),
                java.sql.Timestamp.from(event.executedAt()));
    }

    public List<TradeView> findByUserId(Long userId, int page, int size) {
        String sql = """
                SELECT * FROM trades_view
                WHERE user_id = ?
                ORDER BY executed_at DESC
                LIMIT ? OFFSET ?
                """;
        return jdbc.query(sql, MAPPER, userId, size, page * size);
    }
}