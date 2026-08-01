package com.investmentengine.ingestion.config;

import com.investmentengine.ingestion.model.TradeEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, TradeEvent> consumerFactory() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // ErrorHandlingDeserializer envuelve al deserializador real —
        // si un mensaje llega corrupto o con formato inesperado,
        // no tira el consumer abajo, lo maneja como error recuperable
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                ErrorHandlingDeserializer.class);

        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS,
                StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
                JsonDeserializer.class);

        // Le dice al JsonDeserializer a qué clase Java convertir el JSON
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE,
                "com.investmentengine.ingestion.model.TradeEvent");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.investmentengine.*");

        // group.id identifica este consumer group — todos los pods de este
        // microservicio comparten el mismo group.id, así Kafka reparte
        // las particiones entre ellos sin procesar el mismo mensaje dos veces
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "trade-ingestion-group");

        // earliest = si es la primera vez que este group.id se conecta,
        // lee desde el principio del topic (no se pierde nada histórico)
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Desactivamos el auto-commit automático de offsets.
        // Spring Kafka hace el commit DESPUÉS de que el listener procesa
        // exitosamente el mensaje — así si el proceso falla a mitad,
        // el mensaje se vuelve a entregar en el próximo poll
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TradeEvent>
    kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, TradeEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());

        // 3 threads consumidores en paralelo — coincide con las 3 particiones
        // de trades.raw, así cada thread procesa una partición distinta
        factory.setConcurrency(3);

        return factory;
    }
}