package com.investmentengine.pnl.consumer;

import com.investmentengine.pnl.model.PriceUpdatedEvent;
import com.investmentengine.pnl.service.PnlCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriceUpdatedConsumer {

    private final PnlCalculationService pnlCalculationService;

    @KafkaListener(
            topics = "${kafka.topics.prices-updated}",
            groupId = "pnl-calculator-group"
    )
    public void consume(PriceUpdatedEvent event) {
        log.debug("PriceUpdatedEvent recibido [symbol={}] [price={}]",
                event.symbol(), event.price());
        pnlCalculationService.onPriceUpdated(event);
    }
}