package com.investmentengine.reporting.repository;

import com.investmentengine.reporting.model.PnlCalculatedEvent;
import com.investmentengine.reporting.model.PnlView;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PnlViewRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<PnlView> MAPPER = (rs, rowNum) ->
            new PnlView(
                    rs.getLong("user_id"),
                    rs.getString("symbol"),
                    rs.getBigDecimal("unrealized_pnl"),
                    rs.getBigDecimal("current_price")
            );

    public void upsert(PnlCalculatedEvent event) {
        String sql = """
            INSERT INTO pnl_view (user_id, symbol, unrealized_pnl, current_price, calculated_at)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (user_id, symbol)
            DO UPDATE SET
                unrealized_pnl = EXCLUDED.unrealized_pnl,
                current_price = EXCLUDED.current_price,
                calculated_at = EXCLUDED.calculated_at
            """;
        jdbc.update(sql, event.userId(), event.symbol(),
                event.unrealizedPnl(), event.currentPrice(),
                java.sql.Timestamp.from(event.calculatedAt()));
    }

    public List<PnlView> findByUserId(Long userId) {
        String sql = "SELECT * FROM pnl_view WHERE user_id = ?";
        return jdbc.query(sql, MAPPER, userId);
    }
}