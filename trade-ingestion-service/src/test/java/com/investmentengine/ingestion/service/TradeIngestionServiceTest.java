package com.investmentengine.ingestion.service;

import com.investmentengine.ingestion.model.TradeEvent;
import com.investmentengine.ingestion.model.TradeEvent.TradeType;
import com.investmentengine.ingestion.producer.TradeProcessedProducer;
import com.investmentengine.ingestion.repository.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeIngestionServiceTest {

    @Mock
    private TradeRepository repository;

    @Mock
    private TradeProcessedProducer producer;

    @InjectMocks
    private TradeIngestionService service;

    private TradeEvent validEvent;

    @BeforeEach
    void setUp() {
        validEvent = new TradeEvent(
                UUID.randomUUID().toString(),
                1L,
                "AAPL",
                new BigDecimal("10"),
                new BigDecimal("175.00"),
                TradeType.BUY,
                Instant.now()
        );
    }

    // ESCENARIO 1: trade válido nuevo — debe insertar Y publicar
    @Test
    void process_shouldInsertAndPublish_whenTradeIsNew() {
        // Arrange — el repositorio dice que el trade NO existe
        when(repository.existsByTradeId(validEvent.tradeId())).thenReturn(false);

        // Act
        service.process(validEvent);

        // Assert — verify que se llamó insert y publish exactamente 1 vez
        verify(repository, times(1)).insert(validEvent);
        verify(producer, times(1)).publish(validEvent);
    }

    // ESCENARIO 2: trade duplicado — NO debe insertar ni publicar
    @Test
    void process_shouldSkip_whenTradeIsDuplicate() {
        // Arrange — el repositorio dice que el trade YA existe
        when(repository.existsByTradeId(validEvent.tradeId())).thenReturn(true);

        // Act
        service.process(validEvent);

        // Assert — verify que NUNCA se llamó insert ni publish
        verify(repository, never()).insert(any());
        verify(producer, never()).publish(any());
    }

    // ESCENARIO 3: trade con cantidad inválida — debe lanzar excepción
    @Test
    void process_shouldThrowException_whenQuantityIsZero() {
        // Arrange — trade con cantidad 0
        TradeEvent invalidEvent = new TradeEvent(
                UUID.randomUUID().toString(),
                1L,
                "AAPL",
                BigDecimal.ZERO,        // cantidad inválida
                new BigDecimal("175.00"),
                TradeType.BUY,
                Instant.now()
        );

        // Act + Assert — debe lanzar IllegalArgumentException
        assertThrows(IllegalArgumentException.class,
                () -> service.process(invalidEvent));

        // Nunca debe llegar a insert ni publish
        verify(repository, never()).insert(any());
        verify(producer, never()).publish(any());
    }

    // ESCENARIO 4: trade con precio negativo
    @Test
    void process_shouldThrowException_whenPriceIsNegative() {
        TradeEvent invalidEvent = new TradeEvent(
                UUID.randomUUID().toString(),
                1L,
                "AAPL",
                new BigDecimal("10"),
                new BigDecimal("-1"),   // precio inválido
                TradeType.BUY,
                Instant.now()
        );

        assertThrows(IllegalArgumentException.class,
                () -> service.process(invalidEvent));

        verify(repository, never()).insert(any());
        verify(producer, never()).publish(any());
    }

    // ESCENARIO 5: trade con símbolo vacío
    @Test
    void process_shouldThrowException_whenSymbolIsBlank() {
        TradeEvent invalidEvent = new TradeEvent(
                UUID.randomUUID().toString(),
                1L,
                "",                     // símbolo vacío
                new BigDecimal("10"),
                new BigDecimal("175.00"),
                TradeType.BUY,
                Instant.now()
        );

        assertThrows(IllegalArgumentException.class,
                () -> service.process(invalidEvent));

        verify(repository, never()).insert(any());
        verify(producer, never()).publish(any());
    }
    /**
     * si el event es valido pero si falla en publicarlo
     */

    @Test
    void process_shouldThrowException_when_publishIsFalse() {

        // Arrange — trade válido, no duplicado
        when(repository.existsByTradeId(validEvent.tradeId())).thenReturn(false);

        // Simulamos que el producer explota al publicar
        doThrow(new RuntimeException("Evento no entregado, Algo salio mal"))
        .when(producer).publish(any());



        // Act + Assert — la excepción debe propagarse
        assertThrows(RuntimeException.class, () -> service.process(validEvent));

        // El insert SÍ se llamó — el problema fue después
        verify(repository, times(1)).insert(any());
        // El publish se intentó pero falló
        verify(producer, times(1)).publish(any());




    }
}