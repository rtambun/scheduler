package io.github.rtambun.scheduler.confguration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.rtambun.dto.incident.Incident;
import io.github.rtambun.scheduler.data.dto.IncidentData;
import io.github.rtambun.scheduler.service.CloseIncidentScheduler;
import io.github.rtambun.scheduler.service.client.IncidentProviderClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ElasticSearchInitializerTest {
    private IncidentProviderClient mockIncidentProviderClient;
    private CloseIncidentScheduler mockCloseIncidentScheduler;
    private ElasticSearchInitializer elasticSearchInitializer;

    @BeforeEach
    void setUp() {
        mockIncidentProviderClient = mock(IncidentProviderClient.class);
        mockCloseIncidentScheduler = mock(CloseIncidentScheduler.class);
        elasticSearchInitializer = new ElasticSearchInitializer(mockIncidentProviderClient, mockCloseIncidentScheduler);
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void run_empty_or_null_list_nothing_scheduled(List<Incident> closeIncidentList) throws Exception {
        when(mockIncidentProviderClient.getCloseIncident()).thenReturn(closeIncidentList);

        elasticSearchInitializer.run("");
        verify(mockIncidentProviderClient, times(1)).getCloseIncident();
        verify(mockCloseIncidentScheduler, times(0)).scheduleRemoveCloseIncident(any());
    }

    @Test
    void run_listWithOneElement_ElementScheduled() throws Exception {

        List<Incident> closeIncidentList = List.of(
                new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .readValue(IncidentData.IncidentData1, new TypeReference<Incident>() {})
        );

        when(mockIncidentProviderClient.getCloseIncident()).thenReturn(closeIncidentList);

        elasticSearchInitializer.run("");
        verify(mockIncidentProviderClient, times(1)).getCloseIncident();

        ArgumentCaptor<Incident> closeIncidentArgumentCaptor = ArgumentCaptor.forClass(Incident.class);
        verify(mockCloseIncidentScheduler, times(1))
                .scheduleRemoveCloseIncident(closeIncidentArgumentCaptor.capture());
        assertThat(closeIncidentArgumentCaptor.getValue()).isEqualTo(closeIncidentList.get(0));
    }
}