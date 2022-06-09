package io.github.rtambun.scheduler.confguration;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.HashMap;
import java.util.Map;

@EnableAsync
@Configuration
public class KafkaConfig {

    @Value("${kafka.bootstrap.server1}")
    private String bootstrapServer1;

    @Value("${kafka.bootstrap.server2}")
    private String bootstrapServer2;

    @Value("${kafka.bootstrap.server3}")
    private String bootstrapServer3;

    @Value("${kafka.producer.retries}")
    private int retries;

    @Value("${kafka.client.id}")
    private String clientId;

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer1 + "," + bootstrapServer2 + "," + bootstrapServer3);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.RETRIES_CONFIG, retries);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, "30000000");
        return props;
    }

    @Bean
    public ProducerFactory<String,Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String,Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
