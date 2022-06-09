package io.github.rtambun.scheduler.dto;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class CloseIncidentWithBasicDataTest {

    @Test
    void setName() {
        CloseIncidentWithBasicData closeIncidentWithBasicData = new CloseIncidentWithBasicData();
        assertThat(closeIncidentWithBasicData.getName()).isNull();
        closeIncidentWithBasicData.setName("name");
        assertThat(closeIncidentWithBasicData.getName()).isEqualTo("name");
    }

    @Test
    void setIncidentType() {
        CloseIncidentWithBasicData closeIncidentWithBasicData = new CloseIncidentWithBasicData();
        assertThat(closeIncidentWithBasicData.getIncidentType()).isNull();
        IncidentType incidentType = new IncidentType();
        closeIncidentWithBasicData.setIncidentType(incidentType);
        assertThat(closeIncidentWithBasicData.getIncidentType()).isEqualTo(incidentType);
    }

    @Test
    void setCloseDate() {
        CloseIncidentWithBasicData closeIncidentWithBasicData = new CloseIncidentWithBasicData();
        assertThat(closeIncidentWithBasicData.getCloseDate()).isNull();
        Date date = new Date(System.currentTimeMillis());
        closeIncidentWithBasicData.setCloseDate(date);
        assertThat(closeIncidentWithBasicData.getCloseDate()).isEqualTo(date);
    }

    @Test
    void allArgsConstructor() {
        IncidentType incidentType = new IncidentType();
        Date date = new Date(System.currentTimeMillis());
        CloseIncidentWithBasicData closeIncidentWithBasicData = new CloseIncidentWithBasicData("name",
                incidentType,
                date);
        assertThat(closeIncidentWithBasicData.getName()).isEqualTo("name");
        assertThat(closeIncidentWithBasicData.getIncidentType()).isEqualTo(incidentType);
        assertThat(closeIncidentWithBasicData.getCloseDate()).isEqualTo(date);
    }

}