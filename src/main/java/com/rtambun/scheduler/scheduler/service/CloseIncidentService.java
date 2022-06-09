package com.rtambun.scheduler.scheduler.service;

import com.rtambun.scheduler.scheduler.model.CloseIncident;
import com.rtambun.scheduler.scheduler.repository.CloseIncidentRepository;
import com.rtambun.scheduler.scheduler.service.time.IInstantProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class CloseIncidentService {

    private final CloseIncidentRepository closeIncidentRepository;
    private final IInstantProvider instantProvider;
    private final long minutesBeforeNow;

    public CloseIncidentService(CloseIncidentRepository closeIncidentRepository,
                                IInstantProvider instantProvider,
                                @Value("${incident.close.minutes.before}") long minutesBeforeNow) {
        this.closeIncidentRepository = closeIncidentRepository;
        this.instantProvider = instantProvider;
        this.minutesBeforeNow = minutesBeforeNow;
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

    public List<CloseIncident> findCloseIncidentMinutesBeforeNow() {
        return closeIncidentRepository.findByClosedDateAfter(
                instantProvider.now().minusMillis(minutesBeforeNow * 60 * 1000));
    }
}
