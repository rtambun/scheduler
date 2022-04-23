package com.rtambun.scheduler.scheduler.service;

import com.rtambun.scheduler.scheduler.model.CloseIncident;
import com.rtambun.scheduler.scheduler.repository.CloseIncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CloseIncidentServiceTest {

    private CloseIncidentRepository mockCloseIncidentRepository;
    private CloseIncidentService closeIncidentService;

    @BeforeEach
    public void setUp() {
        mockCloseIncidentRepository = mock(CloseIncidentRepository.class);
        closeIncidentService = new CloseIncidentService(mockCloseIncidentRepository);
    }

    @Test
    void save() {
        CloseIncident closeIncident = new CloseIncident();

        CloseIncident expected = new CloseIncident();
        expected.setId("id");

        when(mockCloseIncidentRepository.save(any())).thenReturn(expected);

        CloseIncident actual = closeIncidentService.save(closeIncident);
        assertThat(actual).isEqualTo(expected);

        ArgumentCaptor<CloseIncident> captorCloseIncident = ArgumentCaptor.forClass(CloseIncident.class);
        verify(mockCloseIncidentRepository, times(1)).save(captorCloseIncident.capture());
        assertThat(captorCloseIncident.getValue()).isEqualTo(closeIncident);
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void saveAll_EmptyOrNull(List<CloseIncident> param) {
        List<CloseIncident> returnList = new ArrayList<>();
        when(mockCloseIncidentRepository.saveAll(any())).thenReturn(returnList);

        List<CloseIncident> actual = closeIncidentService.saveAll(param);
        assertThat(actual.size()).isEqualTo(0);
    }

    @Test
    void saveAllOk() {
        Instant now = Instant.now();
        Instant now1 = Instant.now();
        List<CloseIncident> closeIncidentList = List.of(
                new CloseIncident(null, "type", "severity", now),
                new CloseIncident(null, "type1", "severity1", now1));

        List<CloseIncident> expected = List.of(
                new CloseIncident("id", "type", "severity", Instant.now()),
                new CloseIncident("id1", "type1", "severity1", Instant.now()));
        when(mockCloseIncidentRepository.saveAll(any())).thenReturn(expected);

        List<CloseIncident> actualIncidentList = closeIncidentService.saveAll(closeIncidentList);
        assertThat(actualIncidentList).isEqualTo(expected);
        assertThat(actualIncidentList.size()).isEqualTo(2);
    }
}