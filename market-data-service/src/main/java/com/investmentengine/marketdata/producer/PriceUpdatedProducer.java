package com.investmentengine.marketdata.producer;

import com.investmentengine.marketdata.model.PriceUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriceUpdatedProducer {

    private final KafkaTemplate<String, PriceUpdatedEvent> kafkaTemplate;

    @Value("${kafka.topics.prices-updated}")
    private String pricesUpdatedTopic;

    @PostConstruct
    public void init() {
        log.info("========== CONFIGURACIÓN DEL TOPIC ==========");
        log.info("Topic configurado: '{}'", pricesUpdatedTopic);
        log.info("Longitud del topic: {}", pricesUpdatedTopic.length());
        log.info("Caracteres del topic: {}", Arrays.toString(pricesUpdatedTopic.toCharArray()));
        log.info("¿Es 'prices.updated'? {}", "prices.updated".equals(pricesUpdatedTopic));
        log.info("=============================================");
    }

    public void publish(String symbol, BigDecimal price, Instant updatedAt) {

        PriceUpdatedEvent event = new PriceUpdatedEvent(symbol, price, updatedAt);

        kafkaTemplate.send(pricesUpdatedTopic, symbol, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Error publicando precio [symbol={}]: {}", symbol, ex.getMessage());
                    } else {
                        log.info("Precio publicado [symbol={}] [price={}]", symbol, price);
                    }
                });
    }
}