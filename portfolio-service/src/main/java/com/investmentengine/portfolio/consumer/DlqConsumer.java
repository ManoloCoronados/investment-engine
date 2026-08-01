package com.investmentengine.portfolio.consumer;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.investmentengine.portfolio.model.TradeEvent;
import com.investmentengine.portfolio.repository.DeadLetterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DlqConsumer {

    private final ObjectMapper objectMapper;
    private final DeadLetterRepository deadLetterRepository;

    @KafkaListener(
            topics = "trades.processed.DLT",
            groupId = "portfolio-dlq-group"
    )

    public void consume(ConsumerRecord<String, TradeEvent> record){
        TradeEvent event = record.value();

        log.error("Mensaje en DLQ — trade no procesado [tradeId={}] [userId={}] [symbol={}]",
                event.tradeId(), event.userId(), event.symbol());

        try {


            String payload = objectMapper.writeValueAsString(event);

            // Extraemos el mensaje de error del header que Spring Kafka agrega automáticamente

            String errorMessage = "Error desconocido";
            var errorHeader = record.headers().lastHeader("kafka_dlt-exception-message");
            if (errorHeader != null) {
                errorMessage = new String(errorHeader.value());
            }
            deadLetterRepository.insert(
                    event.tradeId(),
                    event.userId(),
                    event.symbol(),
                    payload,
                    errorMessage
            );
            log.warn("Trade registrado en DLQ [tradeId={}] status=PENDING", event.tradeId());


        }catch (Exception e){
            log.error("Error al guardar en DLQ [tradeId={}]",
                    event.tradeId(), e);
        }

    }







}
