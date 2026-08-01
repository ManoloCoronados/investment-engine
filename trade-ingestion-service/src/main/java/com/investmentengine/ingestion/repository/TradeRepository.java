package com.investmentengine.ingestion.repository;

import com.investmentengine.ingestion.exeption.DuplicateTradeException;
import com.investmentengine.ingestion.model.TradeEvent;
import com.investmentengine.ingestion.model.TradeRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;

@Repository
@RequiredArgsConstructor
public class TradeRepository {
    /**
     * practicamente es un reposiorio que se usa Para persistir los campos de el trade que llegara desde consumer
     * usaremos JDBCTEMPLADE para manejar SQL native
     */

    private final JdbcTemplate jdbc;

    private static final String INSERT_SQL =
      """
      INSERT INTO trades (trade_id, user_id, symbol, quantity, price, trade_type, executed_at)
                                       VALUES (?, ?, ?, ?, ?, ?, ?)
       """;


    /**
     *
     * @param event
     * @return
     * devuelve un tradeRecord yy recibe el objeto tradeEvent mismos campos.
     */
    public TradeRecord insert (TradeEvent event) {

        /**
         * Key holder contenedor que obtiene IDS en tiempo de transaccion evita problemas de lectura
         *
         */
        KeyHolder keyHolder = new GeneratedKeyHolder();
            try {
                /**
                 * metodo para cambiar BD en jdbc
                 *
                 */
                jdbc.update( connection -> {

                            /**
                             * preparedStatement interfaz para para hablar con SQL, seguro
                             * recibe la constante que es una consulte INSERT_SQL y return_GENERATED_KEYS de KeyHolder
                             *
                             */
                            PreparedStatement ps = connection.prepareStatement(INSERT_SQL, new String[]{"id"});

                            /**
                             * Inserta los campos que vienen desde TradeEvent a el ps con setters para persistir
                             */

                            ps.setString(1, event.tradeId());
                    ps.setLong(2, event.userId());
                    ps.setString(3, event.symbol());
                    ps.setBigDecimal(4, event.quantity());
                    ps.setBigDecimal(5, event.price());
                    ps.setString(6, event.tradeType().name());
                    ps.setTimestamp(7, Timestamp.from(event.executedAt()));
                            /**
                             * retorna ps y keyholder(ids)
                             */
                    return ps;
                        }
                        , keyHolder

                );

                /**
                 * exepcion personalizada que realmente garantiza idempotencia gracias a el tradeid y UNIQUE definido en liquibase
                 */
            } catch (DuplicateTradeException ex) {
                throw new DuplicateTradeException(event.tradeId());
            }

        /**
         Explicación: keyHolder.getKey() devuelve el ID generado por la BD (el BIGSERIAL de PostgreSQL).
         Lo usas para crear el TradeRecord con el ID interno. Es correcto y necesario
         */
        Long generatedId = keyHolder.getKey().longValue();

            return new TradeRecord(
                    generatedId,
                    event.tradeId(),
                    event.userId(),
                    event.symbol(),
                    event.quantity(),
                    event.price(),
                    event.tradeType(),
                    event.executedAt()  );
        }


    /**
     * metodo para idempotencia
     * hae consulta  garantiza que no haya  tradeid repetidos
     *
     * @param tradeId
     * @return
     */
    public boolean existsByTradeId (String tradeId) {
            String sql = "SELECT COUNT(*) FROM trades WHERE trade_id = ?";
            Integer count = jdbc.queryForObject(sql, Integer.class, tradeId);
            return count != null && count > 0;
        }



}
