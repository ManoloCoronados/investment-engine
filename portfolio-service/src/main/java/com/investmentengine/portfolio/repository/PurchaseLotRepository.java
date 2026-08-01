package com.investmentengine.portfolio.repository;

import com.investmentengine.portfolio.model.PurchaseLot;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class PurchaseLotRepository {

    private final JdbcTemplate jdbc;

    /**
     RowMapper objeto que uso para convertir filas de postgrest en object java
     */
    private static final RowMapper<PurchaseLot> LOT_MAPPER = (rs, rowNum) -> ///dame un rs y rownum y te devuelvo un PurchsaseLot
            new PurchaseLot(
                    rs.getLong("id"),
                    rs.getLong("user_id"),
                    rs.getString("symbol"),
                    rs.getString("trade_id"),
                    rs.getBigDecimal("original_quantity"),
                    rs.getBigDecimal("remaining_quantity"),
                    rs.getBigDecimal("purchase_price"),
                    rs.getTimestamp("purchased_at").toInstant()
            );

    /**
     *metodo select que devuelve una lista de lote de compras recibe un userId y symbol practicamente es un select de la tabla
     * trae el userid y symbolo y la cantidad de compra se ordena de forma asc para garantizar el patron FIFO
     * practicamente regresa  desde el mas viejo a el mass nuevo
     */
    public List<PurchaseLot> findAvailableLotsFifoOrder(Long userId, String symbol) {
        String sql = """
                SELECT * FROM purchase_lots
                WHERE user_id=? AND symbol=? AND remaining_quantity >0
                ORDER BY purchased_at ASC
                """;
        /**
         * ejecuta la query sql transaction, lowmapper la constante que te ayudara a mapear a object java
         * userid y symbol, parametros que vienen de fuera para que se ejecute el metodo
         */
        return jdbc.query(sql, LOT_MAPPER, userId, symbol);
    }

    /**
     *
     * @param userId
     * @param symbol
     * @param tradeId
     * @param quantity
     * @param price
     * @param purchasedAt
     * @return
     * metodo para insertar una compra recibe los parametros para la transaction , key holder contenedor que guarda
     * los id generados por postgres en tiempo de transaction
     */
    public PurchaseLot insert(Long userId, String symbol, String tradeId,
                              BigDecimal quantity, BigDecimal price, Timestamp purchasedAt) {

        String sql = """
                INSERT INTO purchase_lots (user_id, symbol, trade_id, original_quantity, remaining_quantity,
                                                           purchase_price, purchased_at)
                                                            VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        /**
         * ejecutanos el metodo de jdbc con todos los parametors que necesita el metodo insert para funcionar
         * ps interfaz que abre conexion la consulta, el keyholder y los parametros
         * porque guardamos pasamos el result set en los parametros?
         */

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                    ps.setLong(1, userId);
                    ps.setString(2, symbol);
                    ps.setString(3, tradeId);
                    ps.setBigDecimal(4, quantity);
                    ps.setBigDecimal(5, quantity); // remaining empieza igual al original
                    ps.setBigDecimal(6, price);
                    ps.setTimestamp(7, purchasedAt);
                    return ps;
                }, keyHolder
        );
        /**
         * guardamos el keyholder key y valor junto todos los datos de la compra, pero en donde se guardan?
         */
        Long id = keyHolder.getKey().longValue();
        return new PurchaseLot(id, userId, symbol, tradeId, quantity, quantity, price,
                purchasedAt.toInstant());
    }

    /// Actualiza el estado actual de la compra, cambia el valor viejo metiendo la cantidad de la compra necesita
    /// sql la transsacion el newremaining el valor actual de la accion y lotid  para identificas la accion
    public void updateRemainingQuantity(Long lotId, BigDecimal newRemaining) {
        String sql = "UPDATE purchase_lots SET remaining_quantity = ? WHERE id = ?";
        jdbc.update(sql, newRemaining, lotId);
    }

    /**
     * verificacion rapida de idempotencia devuelve true/false su funcion es evitar usar exepciones de duplicados caras en SQL
     * devuelve un Boleean
     */
    public boolean existsByTradeId(String tradeId) {
        String sql = "SELECT COUNT(*) FROM purchase_lots WHERE trade_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, tradeId);
        return count != null && count > 0;
    }
}