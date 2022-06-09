package io.github.rtambun.scheduler.service;

import io.github.rtambun.scheduler.model.CloseIncident;
import io.github.rtambun.scheduler.repository.CloseIncidentRepository;
import io.github.rtambun.scheduler.service.time.IInstantProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CloseIncidentServiceTest {

    private CloseIncidentRepository mockCloseIncidentRepository;
    private IInstantProvider mockInstantProvider;
    private CloseIncidentService closeIncidentService;
    private static final long TEST_MINUTES_BEFORE_NOW = 5;

    @BeforeEach
    public void setUp() {
        mockCloseIncidentRepository = mock(CloseIncidentRepository.class);
        mockInstantProvider = mock(IInstantProvider.class);
        closeIncidentService = new CloseIncidentService(mockCloseIncidentRepository,
                mockInstantProvider,
                TEST_MINUTES_BEFORE_NOW);
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
                new CloseIncident(null, "name", "severity", "type", now),
                new CloseIncident(null, "name", "severity1", "type1", now1));

        List<CloseIncident> expected = List.of(
                new CloseIncident("id", "name", "severity", "type", Instant.now()),
                new CloseIncident("id1", "name", "severity1", "type1", Instant.now()));
        when(mockCloseIncidentRepository.saveAll(any())).thenReturn(expected);

        List<CloseIncident> actualIncidentList = closeIncidentService.saveAll(closeIncidentList);
        assertThat(actualIncidentList).isEqualTo(expected);
        assertThat(actualIncidentList.size()).isEqualTo(2);
    }

    @Test
    void findCloseIncidentMinutesBeforeNowOk() {
        Instant time = Instant.now()
                .atZone(ZoneOffset.UTC)
                .withHour(1).withMinute((int)TEST_MINUTES_BEFORE_NOW)
                .withSecond(1)
                .withNano(0)
                .toInstant();
        when(mockInstantProvider.now()).thenReturn(time);

        List<CloseIncident> returnList = new ArrayList<>();
        when(mockCloseIncidentRepository.findByCloseDateAfter(any())).thenReturn(returnList);

        List<CloseIncident> actual = closeIncidentService.findCloseIncidentMinutesBeforeNow();
        assertThat(actual.size()).isEqualTo(0);

        verify(mockInstantProvider, times(1)).now();

        ArgumentCaptor<Instant> instantArgumentCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(mockCloseIncidentRepository, times(1))
                .findByCloseDateAfter(instantArgumentCaptor.capture());
        Instant expectedInstant = time
                .atZone(ZoneOffset.UTC)
                .withMinute(0)
                .toInstant();
        assertThat(instantArgumentCaptor.getValue()).isEqualTo(expectedInstant);
    }
}