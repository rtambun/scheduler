package io.github.rtambun.scheduler.confguration;

import io.github.rtambun.dto.incident.Incident;
import io.github.rtambun.scheduler.service.CloseIncidentScheduler;
import io.github.rtambun.scheduler.service.client.IncidentProviderClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Log4j2
public class ElasticSearchInitializer implements CommandLineRunner {

    private final IncidentProviderClient incidentProviderClient;
    private final CloseIncidentScheduler closeIncidentScheduler;
    private final String profile;
    public static final String PROD = "prod";

    public ElasticSearchInitializer(IncidentProviderClient incidentProviderClient,
                                    CloseIncidentScheduler closeIncidentScheduler,
                                    @Value("${spring.profiles.active}") String profile) {
        this.incidentProviderClient = incidentProviderClient;
        this.closeIncidentScheduler = closeIncidentScheduler;
        this.profile = profile;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!profile.equalsIgnoreCase(PROD)) {
            return;
        }

        List<Incident> closeIncidents = incidentProviderClient.getCloseIncident();
        if (closeIncidents != null) {
            closeIncidents.forEach(closeIncidentScheduler::scheduleRemoveCloseIncident);
        }
    }
}
