package com.investmentengine.reporting.consumer;

import com.investmentengine.reporting.model.PortfolioUpdatedEvent;
import com.investmentengine.reporting.repository.PositionViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortfolioUpdatedConsumer {

    private final PositionViewRepository repository;

    @KafkaListener(
            topics = "${kafka.topics.portfolio-updated}",
            groupId = "reporting-service-group",
            containerFactory = "portfolioKafkaListenerContainerFactory"
    )
    public void consume(PortfolioUpdatedEvent event) {
        log.debug("Posición recibida en reporting [userId={}] [symbol={}]",
                event.userId(), event.symbol());
        repository.upsert(event);
    }
}