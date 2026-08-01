package com.investmentengine.ingestion.repository;

import com.investmentengine.ingestion.model.TradeEvent;
import com.investmentengine.ingestion.model.TradeEvent.TradeType;
import com.investmentengine.ingestion.model.TradeRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TradeRepository.class)
class TradeRepositoryTest {

        @Container
        static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("trade_ingestion_db")
                .withUsername("trade_user")
                .withPassword("trade_pass");



        @DynamicPropertySource
        static void configureProperties(DynamicPropertyRegistry registry) {
                registry.add("spring.datasource.url", postgres::getJdbcUrl);
                registry.add("spring.datasource.username", postgres::getUsername);
                registry.add("spring.datasource.password", postgres::getPassword);
        }

        @Autowired
        private TradeRepository repository;

        @Test
        void insert_shouldPersistTrade_whenTradeIsValid() {
                TradeEvent event = new TradeEvent(
                        UUID.randomUUID().toString(),
                        1L,
                        "AAPL",
                        new BigDecimal("10.0000"),
                        new BigDecimal("175.00"),
                        TradeType.BUY,
                        Instant.now()
                );

                TradeRecord result = repository.insert(event);

                assertNotNull(result.id());
                assertEquals(event.tradeId(), result.tradeId());
                assertEquals("AAPL", result.symbol());
                assertEquals(1L, result.userId());
        }

        @Test
        void existsByTradeId_shouldReturnTrue_afterInsert() {
                TradeEvent event = new TradeEvent(
                        UUID.randomUUID().toString(),
                        2L,
                        "TSLA",
                        new BigDecimal("5.0000"),
                        new BigDecimal("245.00"),
                        TradeType.SELL,
                        Instant.now()
                );

                repository.insert(event);

                assertTrue(repository.existsByTradeId(event.tradeId()));
        }

        @Test
        void existsByTradeId_shouldReturnFalse_whenTradeDoesNotExist() {
                String nonExistentId = UUID.randomUUID().toString();

                assertFalse(repository.existsByTradeId(nonExistentId));
        }
}