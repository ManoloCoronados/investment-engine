package com.investmentengine.reporting.consumer;


import com.investmentengine.reporting.model.TradeEvent;
import com.investmentengine.reporting.repository.TradeViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TradeProcessedConsumer {


    private final TradeViewRepository repository;


    @KafkaListener(
            topics = "${kafka.topics.trades-processed}",
            groupId = "reporting-service-group",
            containerFactory = "tradeKafkaListenerContainerFactory"
    )
    public void consume(TradeEvent event){
        log.debug("Trade recibido en reporting [tradeId={}]", event.tradeId());
        repository.insert(event);
    }
}
