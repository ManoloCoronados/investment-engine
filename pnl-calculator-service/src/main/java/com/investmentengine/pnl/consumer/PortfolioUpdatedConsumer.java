package com.investmentengine.pnl.consumer;

import com.investmentengine.pnl.model.PortfolioUpdatedEvent;
import com.investmentengine.pnl.service.PnlCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortfolioUpdatedConsumer {

    private final PnlCalculationService pnlCalculationService;

    @KafkaListener(
            topics = "${kafka.topics.portfolio-updated}",
            groupId = "pnl-calculator-group"
    )
    public void consume(PortfolioUpdatedEvent event) {
        log.debug("PortfolioUpdatedEvent recibido [userId={}] [symbol={}]",
                event.userId(), event.symbol());
        pnlCalculationService.onPortfolioUpdated(event);
    }
}