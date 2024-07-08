package io.mosip.signup.config;

import io.mosip.signup.api.dto.IdentityVerificationResult;
import io.mosip.signup.dto.IdentityVerificationRequest;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaProducerConfig {

    @Value("${kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    @Bean
    public Map<String, Object> producerConfigurations() {
        final Map<String, Object> configurations = new HashMap<>();
        configurations.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        configurations.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configurations.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return configurations;
    }

    @Bean
    public ProducerFactory<String, IdentityVerificationResult> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigurations(),
                new StringSerializer(), new JsonSerializer<IdentityVerificationResult>());
    }

    @Bean
    public KafkaTemplate<String, IdentityVerificationResult> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
