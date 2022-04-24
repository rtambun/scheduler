package com.rtambun.scheduler.scheduler.dto;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CloseIncidentWithBasicDataResponseTest {

    @Test
    void setCount() {
        CloseIncidentWithBasicDataResponse response = new CloseIncidentWithBasicDataResponse();
        assertThat(response.getCount()).isEqualTo(0);
        response.setCount(1);
        assertThat(response.getCount()).isEqualTo(1);
    }

    @Test
    void setIncidents() {
        CloseIncidentWithBasicDataResponse response = new CloseIncidentWithBasicDataResponse();
        assertThat(response.getIncidents()).isNull();
        List<CloseIncidentWithBasicData> incidents = new ArrayList<>();
        response.setIncidents(incidents);
        assertThat(response.getIncidents()).isEqualTo(incidents);
    }

    @Test
    void allArgsConstructor() {
        List<CloseIncidentWithBasicData> incidents = new ArrayList<>();
        CloseIncidentWithBasicDataResponse response = new CloseIncidentWithBasicDataResponse(1, incidents);
        assertThat(response.getCount()).isEqualTo(1);
        assertThat(response.getIncidents()).isEqualTo(incidents);
    }
}