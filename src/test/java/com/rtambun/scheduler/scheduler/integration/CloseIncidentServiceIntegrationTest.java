package com.rtambun.scheduler.scheduler.integration;

import com.rtambun.scheduler.scheduler.model.CloseIncident;
import com.rtambun.scheduler.scheduler.service.CloseIncidentService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(initializers = SchedulerRepositoryContainer.SchedulerRepositoryInitializer.class)
public class CloseIncidentServiceIntegrationTest {

    @BeforeAll
    public static void setEnvironment() {
        SchedulerRepositoryContainer.startSchedulerRepositoryContainer();
    }

    @AfterAll
    public static void destroyEnvironment() {
        SchedulerRepositoryContainer.stopSchedulerRepositoryContainer();
    }

    @Autowired
    private CloseIncidentService closeIncidentService;

    @Test
    public void saveOk() {
        CloseIncident closeIncident = new CloseIncident(null, "type", "severity", Instant.now());
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
                new CloseIncident(null, "type", "severity", Instant.now()),
                new CloseIncident(null, "type1", "severity1", Instant.now()));
        List<CloseIncident> actualIncidentList = closeIncidentService.saveAll(closeIncidentList);
        assertThat(actualIncidentList).isEqualTo(closeIncidentList);
        assertThat(actualIncidentList.size()).isEqualTo(2);
        assertThat(actualIncidentList.get(0).getId()).isNotNull();
        assertThat(actualIncidentList.get(1).getId()).isNotNull();
    }

}
