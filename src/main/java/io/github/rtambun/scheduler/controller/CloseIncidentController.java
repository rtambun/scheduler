package io.github.rtambun.scheduler.controller;

import io.github.rtambun.scheduler.dto.CloseIncidentWithBasicDataResponse;
import io.github.rtambun.scheduler.model.CloseIncident;
import io.github.rtambun.scheduler.service.CloseIncidentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/closeIncident")
public class CloseIncidentController {

    private CloseIncidentService closeIncidentService;

    public CloseIncidentController(CloseIncidentService closeIncidentService) {
        this.closeIncidentService = closeIncidentService;
    }

    @GetMapping("/all")
    public CloseIncidentWithBasicDataResponse getAllCloseIncident() {
        List<CloseIncident> result = closeIncidentService.findCloseIncidentMinutesBeforeNow();
        CloseIncidentWithBasicDataResponse response = new CloseIncidentWithBasicDataResponse();
        response.setCount(result.size());
        return response;
    }

}
