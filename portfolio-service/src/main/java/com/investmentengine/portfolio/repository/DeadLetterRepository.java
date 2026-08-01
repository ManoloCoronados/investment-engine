package com.investmentengine.portfolio.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Repository
public class DeadLetterRepository {

    private final JdbcTemplate jdbc;

    public void insert(
            String tradeId, Long userId, String symbol,
            String payload, String errorMessage
    ) {

        String sql = """
                INSERT INTO dead_letter_trades
                    (trade_id, user_id, symbol, payload, error_message, status)
                VALUES (?, ?, ?, ?, ?, 'PENDING')
                """;

        jdbc.update(sql, tradeId, userId, symbol, payload, errorMessage);
    }


    public void markReprocessed(Long id){

        String sql = """
                UPDATE dead_letter_trades
                SET status = 'REPROCESSED', updated_at = now()
                WHERE id = ?
                """;

        jdbc.update(sql, id);
    }

    public void markCancelled(Long id){

       String sql = """
                UPDATE dead_letter_trades
                SET status = 'CANCELLED', updated_at = now()
                WHERE id = ?
                """;
    }

    public List<Map<String, Object>> findAllPending () {
        String sql = """
                 SELECT * FROM dead_letter_trades
                                WHERE status = 'PENDING'
                                ORDER BY created_at ASC
                                ;
                
                
                """;
                    return   jdbc.queryForList(sql);
    }

}
