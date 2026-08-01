package com.investmentengine.reporting.repository;

import com.investmentengine.reporting.model.PortfolioUpdatedEvent;
import com.investmentengine.reporting.model.PositionView;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PositionViewRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<PositionView> MAPPER = (rs, rowNum) ->
            new PositionView(
                    rs.getLong("user_id"),
                    rs.getString("symbol"),
                    rs.getBigDecimal("total_quantity"),
                    rs.getBigDecimal("average_cost")
            );

    public void upsert (PortfolioUpdatedEvent event)  {
        String sql = """
            INSERT INTO positions_view (user_id, symbol, total_quantity, average_cost, updated_at)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (user_id, symbol)
            DO UPDATE SET
                total_quantity = EXCLUDED.total_quantity,
                average_cost = EXCLUDED.average_cost,
                updated_at = EXCLUDED.updated_at
            """;
        jdbc.update(sql, event.userId(), event.symbol(),
                event.newTotalQuantity(), event.newAverageCost(),
                java.sql.Timestamp.from(event.updatedAt()));
    }

    public List<PositionView> findByUserId(Long userId) {
        String sql = "SELECT * FROM positions_view WHERE user_id = ?";
        return jdbc.query(sql, MAPPER, userId);}
}