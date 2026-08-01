package com.investmentengine.pnl.config;

import com.investmentengine.pnl.model.PortfolioUpdatedEvent;
import com.investmentengine.pnl.model.PriceUpdatedEvent;
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

    // Configuración base común a ambos consumers — evita repetir código
    private Map<String, Object> baseProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.investmentengine.*");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "pnl-calculator-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return props;
    }

    // --- Para portfolio.updated ---

    @Bean
    public ConsumerFactory<String, PortfolioUpdatedEvent> portfolioConsumerFactory() {
        Map<String, Object> props = baseProps();
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE,
                "com.investmentengine.pnl.model.PortfolioUpdatedEvent");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PortfolioUpdatedEvent>
    portfolioKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PortfolioUpdatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(portfolioConsumerFactory());
        factory.setConcurrency(3);
        return factory;
    }

    // --- Para prices.updated ---

    @Bean
    public ConsumerFactory<String, PriceUpdatedEvent> priceConsumerFactory() {
        Map<String, Object> props = baseProps();
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE,
                "com.investmentengine.pnl.model.PriceUpdatedEvent");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PriceUpdatedEvent>
    priceKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PriceUpdatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(priceConsumerFactory());
        // prices.updated tiene solo 1 partición (lo definiste así en docker-compose),
        // así que concurrency=3 no tendría sentido — usar más hilos que particiones
        // significa que algunos hilos nunca van a recibir trabajo
        factory.setConcurrency(1);
        return factory;
    }
}