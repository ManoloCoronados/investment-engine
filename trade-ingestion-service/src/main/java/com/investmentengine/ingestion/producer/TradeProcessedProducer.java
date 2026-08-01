package com.investmentengine.ingestion.producer;

import com.investmentengine.ingestion.model.TradeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradeProcessedProducer {

    private final KafkaTemplate<String, TradeEvent> kafkaTemplate;

    @Value("${kafka.topics.trades-processed}")
    private String tradesProcessedTopic;

    /**
     * Publica un TradeEvent al topic trades-processed
     * Usa userId como key para garantizar orden por usuario
     * Maneja errores con whenComplete
     */
    public void publish(TradeEvent event) {
        String key = event.userId().toString();

        kafkaTemplate.send(tradesProcessedTopic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Error publicando a trades.processed [tradeId={}]: {}",
                                event.tradeId(), ex.getMessage());
                    } else {
                        log.info("Publicado a trades.processed [tradeId={}]", event.tradeId());
                    }
                });
    }
}