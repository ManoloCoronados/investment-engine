package com.investmentengine.ingestion.service;

import com.investmentengine.ingestion.exeption.DuplicateTradeException;
import com.investmentengine.ingestion.model.TradeEvent;
import com.investmentengine.ingestion.producer.TradeProcessedProducer;
import com.investmentengine.ingestion.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeIngestionService {

    private final TradeRepository repository;
    private final TradeProcessedProducer producer;

    public void process(TradeEvent event) {

        validate(event);

        // Chequeo rápido — optimización, no la garantía real
        if (repository.existsByTradeId(event.tradeId())) {
            log.warn("Trade duplicado detectado antes de insertar [tradeId={}]",
                    event.tradeId());
            return;
        }

        try {
            repository.insert(event);
        } catch(DuplicateTradeException ex) {
            // Capturado aquí — esta es la garantía real de idempotencia.
            // Llegó una carrera de concurrencia, la constraint de BD la resolvió.
            log.warn(ex.getMessage());
            return;
        }

        // Solo publicamos si el insert fue exitoso —
        // así garantizamos que trades.processed nunca tiene duplicados
        producer.publish(event);
    }


    /**
     * metodo que valida que los trade tenga sentido
     * @param event
     */
    private void validate(TradeEvent event) {
        if (event.quantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Cantidad inválida para tradeId=" + event.tradeId());
        }
        if (event.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Precio inválido para tradeId=" + event.tradeId());
        }
        if (event.symbol() == null || event.symbol().isBlank()) {
            throw new IllegalArgumentException(
                    "Símbolo vacío para tradeId=" + event.tradeId());
        }
    }
}