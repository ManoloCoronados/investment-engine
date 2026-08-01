package com.investmentengine.portfolio.producer;

import com.investmentengine.portfolio.model.PortfolioUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortfolioUpdatedProducer {

    private final KafkaTemplate<String, PortfolioUpdatedEvent> kafkaTemplate;

    @Value("${kafka.topics.portfolio-updated}")
    private String portfolioUpdatedTopic;

    public void publish(PortfolioUpdatedEvent event) {

        String Key = event.userId().toString();
        kafkaTemplate.send(portfolioUpdatedTopic, Key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Error publicando a portfolio.updated [tradeId={}]: {}",
                            event.tradeId(), ex.getMessage());}
                    else {
                        log.info("Publicado a portfolio.updated [userId={}] [symbol={}] [tradeId={}]",
                                event.userId(), event.symbol(), event.tradeId());
                }


                });
    }
}










