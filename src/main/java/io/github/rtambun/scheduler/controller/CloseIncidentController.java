package io.github.rtambun.scheduler.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.rtambun.dto.incident.Incident;
import io.github.rtambun.scheduler.service.CloseIncidentScheduler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/closeIncident")
public class CloseIncidentController {

    private final CloseIncidentScheduler closeIncidentScheduler;

    public CloseIncidentController(CloseIncidentScheduler closeIncidentScheduler) {
        this.closeIncidentScheduler = closeIncidentScheduler;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Incident>> getAllCloseIncident() {
        try {
            return ResponseEntity
                    .ok()
                    .body(closeIncidentScheduler.getAllScheduledIncident());
        } catch (JsonProcessingException e) {
            return ResponseEntity
                    .internalServerError()
                    .build();
        }
    }
}
