package io.github.rtambun.scheduler.service.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.rtambun.dto.incident.Incident;
import io.github.rtambun.scheduler.data.dto.IncidentData;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IncidentProviderClientTest {
    MockWebServer mockWebServer;
    IncidentProviderClient incidentProviderClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        String path = "/incident/query";
        incidentProviderClient = new IncidentProviderClient(baseUrl, path, 1000);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getCounterResponse_ServerThrow4xx_ThrowClientException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NOT_FOUND.value()));

        ClientException ex = assertThrows(ClientException.class , () -> incidentProviderClient.getCloseIncident());
        assertThat(ex.getStatus()).isEqualTo(ClientException.SERVICE_RESPONSE_STATUS_NON_2XX);
    }

    @Test
    void getCounterResponse_ServerThrow5xx_ThrowClientException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()));

        ClientException ex = assertThrows(ClientException.class , () -> incidentProviderClient.getCloseIncident());
        assertThat(ex.getStatus()).isEqualTo(ClientException.SERVICE_RESPONSE_STATUS_NON_2XX);
    }

    @Test
    void getCounterResponse_ServerSendBackWrongBody() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setBody("wrong data")
                .setHeader("Content-type", "application/json")
        );

        ClientException ex = assertThrows(ClientException.class , () -> incidentProviderClient.getCloseIncident());
        assertThat(ex.getStatus()).isEqualTo(ClientException.SERVICE_REQUEST_FAILED);
    }

    @Test
    void getCounterResponse_ServerTimeOut() {

        mockWebServer.enqueue(new MockResponse()
                .setSocketPolicy(SocketPolicy.NO_RESPONSE));

        ClientException ex = assertThrows(ClientException.class, ()-> incidentProviderClient.getCloseIncident());
        assertThat(ex.getStatus()).isEqualTo(ClientException.SERVICE_REQUEST_FAILED);
    }

    @Test
    void getCounterResponse_ServerSendCorrectMessage() throws JsonProcessingException {
        String jsonPayload ="{\"Count\":\"1\", \"incidents\":" +
                "[" + IncidentData.IncidentData1 + "]}";

        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setResponseCode(HttpStatus.OK.value())
                .setBody(jsonPayload));

        List<Incident> actualCloseIncident = incidentProviderClient.getCloseIncident();
        assertThat(actualCloseIncident.size()).isEqualTo(1);

        List<Incident> expectedCloseIncident = List.of(
                new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .readValue(IncidentData.IncidentData1, new TypeReference<Incident>() {})
        );
        assertThat(actualCloseIncident).usingRecursiveComparison().isEqualTo(expectedCloseIncident);
    }
}