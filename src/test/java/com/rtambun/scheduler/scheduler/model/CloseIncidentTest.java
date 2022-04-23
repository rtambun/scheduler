package com.rtambun.scheduler.scheduler.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CloseIncidentTest {

    @Test
    void setId() {
        CloseIncident closeIncident = new CloseIncident();
        assertThat(closeIncident.getId()).isNull();
        closeIncident.setId("id");
        assertThat(closeIncident.getId()).isEqualTo("id");
    }

    @Test
    void setType() {
        CloseIncident closeIncident = new CloseIncident();
        assertThat(closeIncident.getType()).isNull();
        closeIncident.setType("type");
        assertThat(closeIncident.getType()).isEqualTo("type");
    }

    @Test
    void setSeverity() {
        CloseIncident closeIncident = new CloseIncident();
        assertThat(closeIncident.getSeverity()).isNull();
        closeIncident.setSeverity("severity");
        assertThat(closeIncident.getSeverity()).isEqualTo("severity");
    }

    @Test
    void setClosedDate() {
        CloseIncident closeIncident = new CloseIncident();
        assertThat(closeIncident.getClosedDate()).isNull();
        Instant now = Instant.now();
        closeIncident.setClosedDate(now);
        assertThat(closeIncident.getClosedDate()).isEqualTo(now);
    }

    @Test
    void constructorAllArgs() {
        Instant now = Instant.now();
        CloseIncident closeIncident = new CloseIncident("id" ,"type", "severity", now);
        assertThat(closeIncident.getId()).isEqualTo("id");
        assertThat(closeIncident.getType()).isEqualTo("type");
        assertThat(closeIncident.getSeverity()).isEqualTo("severity");
        assertThat(closeIncident.getClosedDate()).isEqualTo(now);
    }

}