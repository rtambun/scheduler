package io.github.rtambun.scheduler.service.client;

import io.github.rtambun.scheduler.dto.CloseIncidentWithBasicDataResponse;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@Log4j2
public class IncidentProviderClient {

    private String incidentProviderUrl;
    private String closeIncidentWithBasicDataPath;
    private WebClient webClient;

    public IncidentProviderClient(@Value("{incident.close.provider.url}") String incidentProviderUrl,
                                  @Value("{incident.close.provider.closeincidentwithbasicdata}")
                                          String closeIncidentWithBasicDataPath,
                                  ModelMapper modelMapper) {
        this.incidentProviderUrl = incidentProviderUrl;
        this.closeIncidentWithBasicDataPath = closeIncidentWithBasicDataPath;
        webClient = WebClient.create(incidentProviderUrl);
    }

    public Mono<CloseIncidentWithBasicDataResponse> getCloseIncident() {
        return webClient
                .get()
                .uri(closeIncidentWithBasicDataPath)
                .retrieve()
                .onStatus(HttpStatus::isError, status -> {
                    if(status.statusCode().is4xxClientError()) {
                        log.error("Response from " + incidentProviderUrl + "is 4xx");
                    } else {
                        log.error("Response from " + incidentProviderUrl + "is 5xx");
                    }
                    return Mono.error(new ClientException(ClientException.SERVICE_RESPONSE_STATUS_NON_2XX));
                })
                .bodyToMono(CloseIncidentWithBasicDataResponse.class)
                .onErrorMap(Predicate.not(ClientException.class::isInstance), throwable -> {
                    log.error("Failed to send request to service", throwable);
                    return new ClientException(ClientException.SERVICE_REQUEST_FAILED);
                });
    }
}
