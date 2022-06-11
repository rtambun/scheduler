package io.github.rtambun.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.rtambun.dto.kafka.IncidentKafka;
import io.github.rtambun.integration.container.KafkaCloseIncidentContainer;
import io.github.rtambun.integration.container.SchedulerRepositoryContainer;
import io.github.rtambun.integration.mockserver.IncidentProviderMockWebServer;
import io.github.rtambun.scheduler.SchedulerApplication;
import io.github.rtambun.scheduler.data.dto.IncidentData;
import io.github.rtambun.scheduler.util.InstantGenerator;
import lombok.extern.log4j.Log4j2;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SchedulerApplication.class)
@ContextConfiguration(initializers = {
        SchedulerRepositoryContainer.Initializer.class,
        IncidentProviderMockWebServer.Initializer.class,
        KafkaCloseIncidentContainer.Initializer.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EnableAutoConfiguration
@Log4j2
public class IncidentServerRemoveCloseIncidentTest {

    @BeforeAll
    private static void setUp() throws IOException {
        SchedulerRepositoryContainer.startSchedulerRepositoryContainer();

        String jsonPayload ="{\"Count\":\"0\", " +
                "\"incidents\":[]}";

        IncidentProviderMockWebServer
                .startIncidentProviderMockWebServer()
                .getMockWebServer()
                .enqueue(new MockResponse()
                        .setHeader("Content-Type", "application/json")
                        .setResponseCode(HttpStatus.OK.value())
                        .setBody(jsonPayload));

        KafkaCloseIncidentContainer.startKafkaCloseIncidentContainer();
    }

    @AfterAll
    private static void tearDown() throws IOException {
        SchedulerRepositoryContainer.stopSchedulerRepositoryContainer();
        IncidentProviderMockWebServer.stopIncidentProviderMockWebServer();
        KafkaCloseIncidentContainer.stopKafkaCloseIncidentContainer();
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${incident.kafka.topic}")
    private String kafkaTopic;

    @Test
    public void sendMessageToKafkaToReopenClosedIncidentBeforeRemoveBlueIndicatorMessageSend() throws InterruptedException, JsonProcessingException {

        Instant whenGeneratingCloseIncident = Instant.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String closeIncidentDate = LocalDateTime
                .ofInstant(InstantGenerator.generateInstantUTCBeforeMinutes(4), ZoneOffset.UTC)
                .format(dateTimeFormatter);

        String incidentPayload = IncidentData.IncidentData1.replace("\"closeDate\":\"27/04/2022 11:48:54\"",
                "\"closeDate\":\"" + closeIncidentDate + "\"");

        IncidentKafka kafkaPayload = new IncidentKafka();
        kafkaPayload.setPayloadType("Incident");
        kafkaPayload.setPayloadTypeCategory("IncidentStatusUpdated");
        kafkaPayload.setPayload(incidentPayload);

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String mqttPayloadJson = objectMapper.writeValueAsString(kafkaPayload);
        ObjectNode objectNode = (ObjectNode) objectMapper.readTree(mqttPayloadJson);

        kafkaTemplate.send(kafkaTopic, objectNode);

        Thread.sleep(5000);

        incidentPayload = IncidentData.IncidentData1.replace("\"closeDate\":\"27/04/2022 11:48:54\"",
                "\"closeDate\":\"\"")
                .replace("\"status\":\"CLOSED\"",
                        "\"status\":\"OPEN\"");

        kafkaPayload = new IncidentKafka();
        kafkaPayload.setPayloadType("Incident");
        kafkaPayload.setPayloadTypeCategory("IncidentStatusUpdated");
        kafkaPayload.setPayload(incidentPayload);

        mqttPayloadJson = objectMapper.writeValueAsString(kafkaPayload);
        objectNode = (ObjectNode) objectMapper.readTree(mqttPayloadJson);

        kafkaTemplate.send(kafkaTopic, objectNode);

        Duration duration = Duration.between(whenGeneratingCloseIncident, Instant.now());
        log.info("Duration when mock web server started and jobs scheduled - estimated is {}",
                duration.getSeconds());
        Thread.sleep(60 * 1000);

        IncidentKafka expected = new IncidentKafka();
        expected.setPayloadType("Incident");
        expected.setPayloadTypeCategory("IncidentStatusUpdated");
        expected.setPayload(incidentPayload);

        IncidentKafka actual = objectMapper.readValue(this.objectNode.toString(), new TypeReference<>() {
        });

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    private ObjectNode objectNode;

    @KafkaListener(topics = "${incident.kafka.topic}", groupId = "${kafka.client.test.id}")
    public void listenTopic(ObjectNode objectNode) {

        log.info("Message is {}", objectNode);
        this.objectNode = objectNode;
    }
}
