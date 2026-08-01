package com.investmentengine.simulator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.investmentengine.simulator.model.TradeEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, TradeEvent> producerFactory() {

       Map <String, Object> props = new HashMap<>();
        ///Direccion de el BROKER KAFKA
       props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        /** La key del mensaje es String — será el userId para garantizar
        que los trades del mismo usuario siempre vayan a la misma partición  */
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        /**eL Value es JSON - Spring convierte el TrendEvent a json automaticamente
         */
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        /** ACK de todos los brokers — garantiza que el mensaje no se pierde
         En local con 1 broker esto equivale a acks=1, pero es buena práctica */
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        /**
         * reitentos automaticos si kafka no responde
         */
        props.put(ProducerConfig.RETRIES_CONFIG, 3);

        ///Espera a que el batch se llene a 16k
        ///tamano de el batch agrupa mensajes para mayor troughout
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);

        /**
         * tiempo de espera  5 milisegundos
         */
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);

        // No incluir el header con el tipo de clase Java en el mensaje
        // Si lo incluyes, el consumer necesita las mismas clases — acoplamiento innecesario
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);


        return new DefaultKafkaProducerFactory<>(props,
                new StringSerializer(),
                new JsonSerializer<>(objectMapper()));
    }
    ///crea el bean templade y envuelve el producer factory
    @Bean
    KafkaTemplate <String, TradeEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
        return mapper;
    }
}
