package com.rtambun.scheduler.scheduler.service;

import com.rtambun.scheduler.scheduler.model.CloseIncident;
import com.rtambun.scheduler.scheduler.repository.CloseIncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
}