package io.github.rtambun.scheduler.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.rtambun.dto.incident.Incident;
import io.github.rtambun.scheduler.service.CloseIncidentScheduler;
import io.github.rtambun.scheduler.service.time.InstantProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CloseIncidentControllerTest {

    private CloseIncidentScheduler closeIncidentScheduler;
    private CloseIncidentController closeIncidentController;

    @BeforeEach
    public void setUp() {
        closeIncidentScheduler = mock(CloseIncidentScheduler.class);
        closeIncidentController = new CloseIncidentController(closeIncidentScheduler);
    }

    @Test
    public void getAllCloseIncident_ok() throws JsonProcessingException {
        List<Incident> result = new ArrayList<>();
        when(closeIncidentScheduler.getAllScheduledIncident()).thenReturn(result);

        ResponseEntity<List<Incident>> actual = closeIncidentController.getAllCloseIncident();
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual.getBody()).usingRecursiveComparison().isEqualTo(new ArrayList<Incident>());
    }

    @Test
    public void getAllCloseIncident_jsonProcessingException_ReturnHttpInternalServerError() throws JsonProcessingException {
        List<Incident> result = new ArrayList<>();
        when(closeIncidentScheduler.getAllScheduledIncident()).thenThrow(new JsonProcessingException("any"){});

        ResponseEntity<List<Incident>> actual = closeIncidentController.getAllCloseIncident();
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(actual.getBody()).isNull();
    }
}