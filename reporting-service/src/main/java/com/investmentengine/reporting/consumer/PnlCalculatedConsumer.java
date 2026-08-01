package com.investmentengine.reporting.consumer;

import com.investmentengine.reporting.model.PnlCalculatedEvent;
import com.investmentengine.reporting.repository.PnlViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PnlCalculatedConsumer {

    private final PnlViewRepository repository;

    @KafkaListener(
            topics = "${kafka.topics.pnl-calculated}",
            groupId = "reporting-service-group",
            containerFactory = "pnlKafkaListenerContainerFactory"
    )
    public void consume(PnlCalculatedEvent event) {
        log.debug("P&L recibido en reporting [userId={}] [symbol={}]",
                event.userId(), event.symbol());
        repository.upsert(event);
    }
}