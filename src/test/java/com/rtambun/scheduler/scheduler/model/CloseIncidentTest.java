package com.rtambun.scheduler.scheduler.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CloseIncidentTest {

    @Test
    void setConstructorNoArgs() {
        CloseIncident closeIncident = new CloseIncident();
        assertThat(closeIncident.getId()).isNull();
        assertThat(closeIncident.getName()).isNull();
        assertThat(closeIncident.getSeverity()).isNull();
        assertThat(closeIncident.getType()).isNull();
        assertThat(closeIncident.getCloseDate()).isNull();

        closeIncident.setId("id");
        closeIncident.setName("name");
        closeIncident.setSeverity("severity");
        closeIncident.setType("type");
        Instant now = Instant.now();
        closeIncident.setCloseDate(now);

        assertThat(closeIncident.getId()).isEqualTo("id");
        assertThat(closeIncident.getName()).isEqualTo("name");
        assertThat(closeIncident.getSeverity()).isEqualTo("severity");
        assertThat(closeIncident.getType()).isEqualTo("type");
        assertThat(closeIncident.getCloseDate()).isEqualTo(now);
    }

    @Test
    void constructorAllArgs() {
        Instant now = Instant.now();
        CloseIncident closeIncident = new CloseIncident("id", "name", "severity", "type", now);

        assertThat(closeIncident.getId()).isEqualTo("id");
        assertThat(closeIncident.getName()).isEqualTo("name");
        assertThat(closeIncident.getSeverity()).isEqualTo("severity");
        assertThat(closeIncident.getType()).isEqualTo("type");
        assertThat(closeIncident.getCloseDate()).isEqualTo(now);
    }

}