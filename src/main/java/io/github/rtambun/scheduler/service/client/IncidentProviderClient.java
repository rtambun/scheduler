package io.github.rtambun.scheduler.service.client;

import io.github.rtambun.dto.IncidentQuery;
import io.github.rtambun.dto.incident.Incident;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Service
@Log4j2
public class IncidentProviderClient {

    private final String incidentProviderUrl;
    private final String closeIncidentWithBasicDataPath;
    private final WebClient webClient;

    public IncidentProviderClient(@Value("{incident.close.provider.url}") String incidentProviderUrl,
                                  @Value("{incident.close.provider.path}")
                                          String closeIncidentWithBasicDataPath,
                                  @Value("{incident.close.provider.timeout}") int connectionTimeOut) {
        this.incidentProviderUrl = incidentProviderUrl;
        this.closeIncidentWithBasicDataPath = closeIncidentWithBasicDataPath;

        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(connectionTimeOut));

        webClient = WebClient.builder()
                .baseUrl(incidentProviderUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    public List<Incident> getCloseIncident() {
        IncidentQuery response = webClient
                .get()
                .uri(closeIncidentWithBasicDataPath)
                .retrieve()
                .onStatus(HttpStatus::isError, status -> {
                    if(status.statusCode().is4xxClientError()) {
                        log.error("Response from " + incidentProviderUrl + "/" + closeIncidentWithBasicDataPath + " is 4xx");
                    } else {
                        log.error("Response from " + incidentProviderUrl + "/" + closeIncidentWithBasicDataPath + "is 5xx");
                    }
                    return Mono.error(new ClientException(ClientException.SERVICE_RESPONSE_STATUS_NON_2XX));
                })
                .bodyToMono(IncidentQuery.class)
                .onErrorMap(Predicate.not(ClientException.class::isInstance), throwable -> {
                    log.error("Failed to send request to service", throwable);
                    return new ClientException(ClientException.SERVICE_REQUEST_FAILED);})
                .block();
        if (response == null || response.getIncidents() == null) {
            return new ArrayList<>();
        }
        return response.getIncidents();
    }
}
