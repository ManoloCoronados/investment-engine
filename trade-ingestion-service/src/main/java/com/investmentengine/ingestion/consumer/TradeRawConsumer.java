package com.investmentengine.ingestion.consumer;

import com.investmentengine.ingestion.model.TradeEvent;
import com.investmentengine.ingestion.service.TradeIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradeRawConsumer {

    private final TradeIngestionService ingestionService;


    @KafkaListener(
            topics = "${kafka.topics.trades-raw}",
            groupId = "trade-ingestion-group")
    public void consume(TradeEvent event) {
        log.debug("Mensaje recibido de trades.raw [tradeId={}]", event.tradeId());

        try {
            ingestionService.process(event);
        }catch (IllegalArgumentException ex) {
            log.error("Trade inválido descartado: {}", ex.getMessage());
        }

    }

}
