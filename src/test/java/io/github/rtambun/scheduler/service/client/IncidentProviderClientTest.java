package io.github.rtambun.scheduler.service.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rtambun.scheduler.dto.CloseIncidentWithBasicDataResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class IncidentProviderClientTest {
    MockWebServer mockWebServer;
    IncidentProviderClient incidentProviderClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        String closeIncidentWithBasicDataPath = "/closeIncidentWithBasicData";
        incidentProviderClient = new IncidentProviderClient(baseUrl, closeIncidentWithBasicDataPath, new ModelMapper());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getCounterResponse_ServerThrow4xx_ThrowClientException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NOT_FOUND.value()));

        Mono<CloseIncidentWithBasicDataResponse> response = incidentProviderClient.getCloseIncident();
        StepVerifier.create(response)
                .expectSubscription()
                .expectErrorMatches(ex -> ex instanceof ClientException
                        && ((ClientException) ex).getStatus() == ClientException.SERVICE_RESPONSE_STATUS_NON_2XX)
                .verify();
    }

    @Test
    void getCounterResponse_ServerThrow5xx_ThrowClientException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()));

        Mono<CloseIncidentWithBasicDataResponse> response = incidentProviderClient.getCloseIncident();
        StepVerifier.create(response)
                .expectSubscription()
                .expectErrorMatches(ex -> ex instanceof ClientException
                        && ((ClientException) ex).getStatus() == ClientException.SERVICE_RESPONSE_STATUS_NON_2XX)
                .verify();
    }

    @Test
    void getCounterResponse_ServerSendBackWrongBody() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setBody("wrong data")
        );

        Mono<CloseIncidentWithBasicDataResponse> response = incidentProviderClient.getCloseIncident();
        StepVerifier.create(response)
                .expectSubscription()
                .expectErrorMatches(ex -> ex instanceof ClientException
                        && ((ClientException) ex).getStatus() == ClientException.SERVICE_REQUEST_FAILED)
                .verify();
    }

    @Test
    void getCounterResponse_ServerSendCorrectMessage() throws JsonProcessingException {
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(HttpStatus.OK.value())
                .setBody(new ObjectMapper()
                        .writeValueAsString(new CloseIncidentWithBasicDataResponse(1, new ArrayList<>()))));

        Mono<CloseIncidentWithBasicDataResponse> response = incidentProviderClient.getCloseIncident();
        StepVerifier.create(response)
                .expectSubscription()
                .assertNext(result -> assertThat(result)
                        .usingRecursiveComparison()
                        .isEqualTo(new CloseIncidentWithBasicDataResponse(1,new ArrayList<>())))
                .verifyComplete();
    }
}