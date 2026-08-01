package com.investmentengine.portfolio.consumer;

import com.investmentengine.portfolio.model.TradeEvent;
import com.investmentengine.portfolio.service.FifoPortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradeProcessedConsumer {

    private final FifoPortfolioService fifoPortfolioService;

    @KafkaListener(
            topics = "${kafka.topics.trades-processed}",
            groupId = "portfolio-service-group"
    )
    public void consumer(TradeEvent event){
        log.debug("Trade recibido en portfolio-service [tradeId={}] [type={}]",
                event.tradeId(), event.tradeType());

        fifoPortfolioService.process(event);
    }






}
