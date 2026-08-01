package com.investmentengine.simulator.producer;


import com.investmentengine.simulator.model.TradeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradeEventProducer {

    private final KafkaTemplate<String, TradeEvent> kafkaTemplate;

    @Value("${kafka.topics.trades-raw}")
    private String tradesTopic;

    /**
     Objetivo de la clase es enviar el evento a el broker de kafka, creamos publish void y iniciamos un objeto de la clase competableFuture junto
     con el metodo de kafka, o cual nos proporiona whencpletable con result y execpion para manejar errores y exitos
     */
    public void publish(TradeEvent event) {
        String key = event.userId().toString();

        CompletableFuture<SendResult<String, TradeEvent>> future =
                kafkaTemplate.send(tradesTopic, key, event);
        /**
         * whemComplete necesita de ACK para confirmar que el event se guardo en El broker
         */
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                // ✅ Quité las comillas dobles extra
                log.error("ERROR SENDING TRADE, [tradeId={}] FOR USER [userId={}]: {}",
                        event.tradeId(), event.userId(), ex.getMessage());
            } else {
                log.info("Trade publicado [tradeId={}] [userId={}] [symbol={}] " +
                                "[type={}] [price={}] -> partición {}",
                        event.tradeId(),
                        event.userId(),
                        event.symbol(),
                        event.tradeType(),
                        event.price(),
                        result.getRecordMetadata().partition());
            }
        });
    }
}