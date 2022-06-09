package io.github.rtambun.integration.container;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class KafkaCloseIncidentContainer extends KafkaContainer {

    //see https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/ for reference of elastic search
    //version for your spring boot
    private static final String KAFKA_VERSION = "6.2.1";

    private static KafkaCloseIncidentContainer kafkaCloseIncidentContainer;
    private static String bootStrapServer;

    public static void startKafkaCloseIncidentContainer() {
        if (kafkaCloseIncidentContainer == null) {
            kafkaCloseIncidentContainer = new KafkaCloseIncidentContainer();
            kafkaCloseIncidentContainer.start();
            bootStrapServer = kafkaCloseIncidentContainer.getBootstrapServers();
        }
    }

    public static void stopKafkaCloseIncidentContainer() {
        kafkaCloseIncidentContainer.stop();
        kafkaCloseIncidentContainer = null;
    }

    private KafkaCloseIncidentContainer() {
        super(DockerImageName.parse("confluentinc/cp-kafka:"+ KAFKA_VERSION));
    }

    public static class Initializer implements
            ApplicationContextInitializer<ConfigurableApplicationContext> {


        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            String kafkaBootStrapServer1 = "kafka.bootstrap.server1=" + bootStrapServer;
            String kafkaBootStrapServer2 = "kafka.bootstrap.server2=" + bootStrapServer;
            String kafkaBootStrapServer3 = "kafka.bootstrap.server3=" + bootStrapServer;
            String kafkaProducerRetries = "kafka.producer.retries=3";
            String kafkaClientId = "kafka.client.id=ismsagent";
            String kafkaClientIdForTest = "kafka.client.test.id=test";
            String ismsKafkaTopic = "isms.kafka.topic=inc";

            TestPropertyValues.of(kafkaBootStrapServer1).applyTo(applicationContext.getEnvironment());
            TestPropertyValues.of(kafkaBootStrapServer2).applyTo(applicationContext.getEnvironment());
            TestPropertyValues.of(kafkaBootStrapServer3).applyTo(applicationContext.getEnvironment());
            TestPropertyValues.of(kafkaProducerRetries).applyTo(applicationContext.getEnvironment());
            TestPropertyValues.of(kafkaClientId).applyTo(applicationContext.getEnvironment());
            TestPropertyValues.of(kafkaClientIdForTest).applyTo(applicationContext.getEnvironment());
            TestPropertyValues.of(ismsKafkaTopic).applyTo(applicationContext.getEnvironment());
        }
    }
}
