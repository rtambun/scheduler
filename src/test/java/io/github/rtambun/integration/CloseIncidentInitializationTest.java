package io.github.rtambun.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.rtambun.dto.incident.Incident;
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
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
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
public class CloseIncidentInitializationTest {

    private static Instant closeIncidentInstant;
    private static Instant whenGeneratingCloseIncident;
    private static String incidentPayload;

    @BeforeAll
    private static void setUp() throws IOException {
        SchedulerRepositoryContainer.startSchedulerRepositoryContainer();
        closeIncidentInstant = InstantGenerator.generateInstantUTCBeforeMinutes(4);
        whenGeneratingCloseIncident = Instant.now();

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String closeIncidentDate = LocalDateTime
                .ofInstant(closeIncidentInstant, ZoneOffset.UTC)
                .format(dateTimeFormatter);

       incidentPayload = IncidentData.IncidentData1.replace("\"closeDate\":\"27/04/2022 11:48:54\"",
                "\"closeDate\":\"" + closeIncidentDate + "\"");

        String jsonPayload ="{\"Count\":\"1\", " +
                "\"incidents\":["+ incidentPayload +"]}";

        IncidentProviderMockWebServer
                .startIsmsMockWebServer()
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
        IncidentProviderMockWebServer.stopIsmsMockWebServer();
        KafkaCloseIncidentContainer.stopKafkaCloseIncidentContainer();
    }

    @Test
    public void initializeWithOneCloseIncident() throws InterruptedException, JsonProcessingException {

        Duration duration = Duration.between(whenGeneratingCloseIncident, Instant.now());
        log.info("Duration when mock web server started and jobs scheduled - estimated is {}",
                duration.getSeconds());
        Thread.sleep(60 * 1000);

        IncidentKafka expected = new IncidentKafka();
        expected.setPayloadType("Incident");
        expected.setPayloadTypeCategory("RemovedBlueIndicatorFromMap");
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        IncidentKafka actual = objectMapper.readValue(objectNode.toString(), new TypeReference<>() {
        });

        assertThat(actual).usingRecursiveComparison().ignoringFields("payload").isEqualTo(expected);
        assertThat(objectMapper.readValue(actual.getPayload(), new TypeReference<Incident>() {}))
                .usingRecursiveComparison()
                .isEqualTo(objectMapper.readValue(incidentPayload, new TypeReference<Incident>() {}));
    }

    private ObjectNode objectNode;

    @KafkaListener(topics = "${isms.kafka.topic}", groupId = "${kafka.client.test.id}")
    public void listenTopic(ObjectNode objectNode) {

        log.info("Message is {}", objectNode);
        this.objectNode = objectNode;
    }
}
