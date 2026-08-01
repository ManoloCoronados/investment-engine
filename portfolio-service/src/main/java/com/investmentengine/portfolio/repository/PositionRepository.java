package com.investmentengine.portfolio.repository;

import com.investmentengine.portfolio.model.Position;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class PositionRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<Position> POSITION_MAPPER = (rs, rowNum) ->
            new Position(
                    rs.getLong("user_id"),
                    rs.getString("symbol"),
                    rs.getBigDecimal("total_quantity"),
                    rs.getBigDecimal("average_cost")
            );

    public Optional<Position> findByUserAndSymbol(Long userId, String symbol) {
        String sql = "SELECT * FROM positions WHERE user_id = ? AND symbol = ?";
        List<Position> results = jdbc.query(sql, POSITION_MAPPER, userId, symbol);
        return results.stream().findFirst();
    }

    public void upsert(Long userId, String symbol, BigDecimal totalQuantity, BigDecimal averageCost) {
        String sql = """
                INSERT INTO positions (user_id, symbol, total_quantity, average_cost, updated_at)
                VALUES (?, ?, ?, ?, now())
                ON CONFLICT (user_id, symbol)
                DO UPDATE SET
                    total_quantity = EXCLUDED.total_quantity,
                    average_cost = EXCLUDED.average_cost,
                    updated_at = now()
                """;
        jdbc.update(sql, userId, symbol, totalQuantity, averageCost);
    }
}