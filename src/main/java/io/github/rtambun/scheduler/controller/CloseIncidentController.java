package io.github.rtambun.scheduler.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.rtambun.dto.incident.Incident;
import io.github.rtambun.scheduler.service.CloseIncidentScheduler;
import io.github.rtambun.scheduler.service.time.InstantProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/closeIncident")
public class CloseIncidentController {

    private CloseIncidentScheduler closeIncidentScheduler;
    private InstantProvider instantProvider;

    public CloseIncidentController(CloseIncidentScheduler closeIncidentScheduler,
                                   InstantProvider instantProvider) {
        this.closeIncidentScheduler = closeIncidentScheduler;
        this.instantProvider = instantProvider;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Incident>> getAllCloseIncident() {
        try {
            return ResponseEntity
                    .ok()
                    .body(closeIncidentScheduler.getAllScheduledIncident(instantProvider.now()));
        } catch (JsonProcessingException e) {
            return ResponseEntity
                    .internalServerError()
                    .build();
        }
    }
}
