package io.github.rtambun.integration;

import io.github.rtambun.integration.container.KafkaCloseIncidentContainer;
import io.github.rtambun.integration.container.SchedulerRepositoryContainer;
import io.github.rtambun.integration.mockserver.IsmsMockWebServer;
import io.github.rtambun.scheduler.SchedulerApplication;
import io.github.rtambun.scheduler.model.CloseIncident;
import io.github.rtambun.scheduler.repository.CloseIncidentRepository;
import io.github.rtambun.scheduler.service.CloseIncidentService;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SchedulerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {
        SchedulerRepositoryContainer.Initializer.class,
        IsmsMockWebServer.Initializer.class,
        KafkaCloseIncidentContainer.Initializer.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CloseIncidentServiceIntegrationTest {

    @BeforeAll
    public static void setEnvironment() throws IOException {
        SchedulerRepositoryContainer.startSchedulerRepositoryContainer();
        IsmsMockWebServer
                .startIsmsMockWebServer()
                .getMockWebServer()
                .enqueue(new MockResponse()
                        .setHeader("Content-Type", "application/json")
                        .setResponseCode(HttpStatus.OK.value())
                        .setBody("{\"Count\":\"0\", \"incidents\":[]}"));
        KafkaCloseIncidentContainer.startKafkaCloseIncidentContainer();
    }

    @AfterAll
    public static void destroyEnvironment() throws IOException {
        SchedulerRepositoryContainer.stopSchedulerRepositoryContainer();
        IsmsMockWebServer.stopIsmsMockWebServer();
        KafkaCloseIncidentContainer.stopKafkaCloseIncidentContainer();
    }

    @BeforeEach
    public void setUp() {
        closeIncidentRepository.deleteAll();
    }

    @Autowired
    private CloseIncidentRepository closeIncidentRepository;

    @Autowired
    private CloseIncidentService closeIncidentService;

    @Test
    public void saveOk() {
        CloseIncident closeIncident = new CloseIncident(null, "name", "severity", "type", Instant.now());
        CloseIncident actual = closeIncidentService.save(closeIncident);
        assertThat(actual).isEqualTo(closeIncident);
        assertThat(actual.getId()).isNotNull();
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    public void saveAll_EmptyOrNull(List<CloseIncident> param) {
        List<CloseIncident> actualIncidentList = closeIncidentService.saveAll(param);
        assertThat(actualIncidentList.size()).isEqualTo(0);
    }

    @Test
    public void saveAllOk() {
        List<CloseIncident> closeIncidentList = List.of(
                new CloseIncident(null, "name", "severity", "type", Instant.now()),
                new CloseIncident(null, "name", "severity1", "type1", Instant.now()));
        List<CloseIncident> actualIncidentList = closeIncidentService.saveAll(closeIncidentList);
        assertThat(actualIncidentList).isEqualTo(closeIncidentList);
        assertThat(actualIncidentList.size()).isEqualTo(2);
        assertThat(actualIncidentList.get(0).getId()).isNotNull();
        assertThat(actualIncidentList.get(1).getId()).isNotNull();
    }

    @Test
    public void findCloseIncidentMinutesBeforeNow_Ok() {
        List<CloseIncident> actualCloseIncidentList = closeIncidentService.findCloseIncidentMinutesBeforeNow();
        assertThat(actualCloseIncidentList.size()).isEqualTo(0);

        CloseIncident closeIncidentOld = new CloseIncident(
                null,
                "name",
                "severity",
                "type",
                Instant.now()
                        .minusMillis((SchedulerRepositoryContainer.MINUTES_BEFORE_NOW + 1) * 60 * 1000));
        CloseIncident saved = closeIncidentService.save(closeIncidentOld);
        assertThat(saved).isEqualTo(closeIncidentOld);
        assertThat(saved.getId()).isNotNull();

        CloseIncident closeIncident = new CloseIncident(null, "name", "severity", "type", Instant.now());
        CloseIncident expected = closeIncidentService.save(closeIncident);
        assertThat(expected).isEqualTo(closeIncident);
        assertThat(expected.getId()).isNotNull();

        actualCloseIncidentList = closeIncidentService.findCloseIncidentMinutesBeforeNow();
        assertThat(actualCloseIncidentList.size()).isEqualTo(1);
        assertThat(actualCloseIncidentList.get(0)).usingRecursiveComparison().isEqualTo(expected);
    }
}
