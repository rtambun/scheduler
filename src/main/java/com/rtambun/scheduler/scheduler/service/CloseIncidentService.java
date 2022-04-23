package com.rtambun.scheduler.scheduler.service;

import com.rtambun.scheduler.scheduler.model.CloseIncident;
import com.rtambun.scheduler.scheduler.repository.CloseIncidentRepository;
import org.springframework.stereotype.Service;

@Service
public class CloseIncidentService {

    private CloseIncidentRepository closeIncidentRepository;

    public CloseIncidentService(CloseIncidentRepository closeIncidentRepository) {
        this.closeIncidentRepository = closeIncidentRepository;
    }

    public CloseIncident save (CloseIncident closeIncident) {
        return closeIncidentRepository.save(closeIncident);
    }

}
