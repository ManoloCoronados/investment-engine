package com.investmentengine.pnl.repository;

import com.investmentengine.pnl.model.PositionSnapshot;
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
public class PositionSnapshotRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<PositionSnapshot> MAPPER = (rs, rowNum) ->
            new PositionSnapshot(
                    rs.getLong("user_id"),
                    rs.getString("symbol"),
                    rs.getBigDecimal("total_quantity"),
                    rs.getBigDecimal("average_cost")
            );

    // Busca la posición actual de un usuario en un símbolo —
    // se usa cuando llega un PriceUpdatedEvent y necesitas saber
    // quién tiene ese símbolo para recalcular su P&L
    public Optional<PositionSnapshot> findByUserAndSymbol(Long userId, String symbol) {
        String sql = "SELECT * FROM positions_snapshot WHERE user_id = ? AND symbol = ?";
        List<PositionSnapshot> results = jdbc.query(sql, MAPPER, userId, symbol);
        return results.stream().findFirst();
    }

    /**
     *
     *busca a usuarios que tienen position en un simbolo
     * ya que un priceUpdated puede afectar a todos
     */
    public List<PositionSnapshot> findAllBySymbol(String symbol) {
        String sql = "SELECT * FROM positions_snapshot WHERE symbol = ?";
        return jdbc.query(sql, MAPPER, symbol);
    }

    // Mantiene esta tabla sincronizada con lo que publica portfolio-service
    public void upsert(Long userId, String symbol, BigDecimal totalQuantity,
                       BigDecimal averageCost, Instant updatedAt) {
        String sql = """
            INSERT INTO positions_snapshot (user_id, symbol, total_quantity, average_cost, updated_at)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (user_id, symbol)
            DO UPDATE SET
                total_quantity = EXCLUDED.total_quantity,
                average_cost = EXCLUDED.average_cost,
                updated_at = EXCLUDED.updated_at
            """;
        jdbc.update(sql, userId, symbol, totalQuantity, averageCost,
                java.sql.Timestamp.from(updatedAt));
    }
}