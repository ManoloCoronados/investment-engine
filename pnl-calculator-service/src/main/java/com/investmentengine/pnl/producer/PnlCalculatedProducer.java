package com.investmentengine.pnl.producer;

import com.investmentengine.pnl.model.PnlCalculatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PnlCalculatedProducer {

    private final KafkaTemplate<String, PnlCalculatedEvent> kafkaTemplate;

    @Value("${kafka.topics.pnl-calculated}")
    private String pnlCalculatedTopic;

    public void publish(PnlCalculatedEvent event) {
        String key = event.userId().toString();

        kafkaTemplate.send(pnlCalculatedTopic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Error publicando P&L [userId={}] [symbol={}]: {}",
                                event.userId(), event.symbol(), ex.getMessage());
                    } else {
                        log.info("P&L publicado [userId={}] [symbol={}] [P&L={}]",
                                event.userId(), event.symbol(), event.unrealizedPnl());
                    }
                });
    }
}