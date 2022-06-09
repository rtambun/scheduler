package io.github.rtambun.scheduler.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IncidentTypeTest {

    @Test
    void setType() {
        IncidentType incidentType = new IncidentType();
        assertThat(incidentType.getType()).isNull();
        incidentType.setType("type");
        assertThat(incidentType.getType()).isEqualTo("type");
    }

    @Test
    void setSeverity() {
        IncidentType incidentType = new IncidentType();
        assertThat(incidentType.getSeverity()).isNull();
        incidentType.setSeverity("severity");
        assertThat(incidentType.getSeverity()).isEqualTo("severity");
    }

    @Test
    void constructorAllArgs() {
        IncidentType incidentType = new IncidentType("type", "severity");
        assertThat(incidentType.getType()).isEqualTo("type");
        assertThat(incidentType.getSeverity()).isEqualTo("severity");
    }
}