package io.github.rtambun.scheduler.confguration;

import io.github.rtambun.dto.incident.Incident;
import io.github.rtambun.scheduler.service.CloseIncidentScheduler;
import io.github.rtambun.scheduler.service.client.IncidentProviderClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Log4j2
public class ElasticSearchInitializer implements CommandLineRunner {

    private final IncidentProviderClient ismsEpClient;
    private final CloseIncidentScheduler closeIncidentScheduler;

    public ElasticSearchInitializer(IncidentProviderClient incidentProviderClient,
                                    CloseIncidentScheduler closeIncidentScheduler) {
        this.ismsEpClient = incidentProviderClient;
        this.closeIncidentScheduler = closeIncidentScheduler;
    }

    @Override
    public void run(String... args) throws Exception {
        List<Incident> closeIncidents = ismsEpClient.getCloseIncident();
        if (closeIncidents != null) {
            closeIncidents.forEach(closeIncidentScheduler::scheduleRemoveCloseIncident);
        }
    }
}
