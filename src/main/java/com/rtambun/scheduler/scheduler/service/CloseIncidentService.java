package com.rtambun.scheduler.scheduler.service;

import com.rtambun.scheduler.scheduler.model.CloseIncident;
import com.rtambun.scheduler.scheduler.repository.CloseIncidentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class CloseIncidentService {

    private CloseIncidentRepository closeIncidentRepository;

    public CloseIncidentService(CloseIncidentRepository closeIncidentRepository) {
        this.closeIncidentRepository = closeIncidentRepository;
    }

    public CloseIncident save (CloseIncident closeIncident) {
        return closeIncidentRepository.save(closeIncident);
    }

    public List<CloseIncident> saveAll (List<CloseIncident> closeIncidentList) {
        if (closeIncidentList == null)
            return new ArrayList<>();

        return StreamSupport
                .stream(closeIncidentRepository.saveAll(closeIncidentList).spliterator(), false)
                .collect(Collectors.toList());
    }

}
