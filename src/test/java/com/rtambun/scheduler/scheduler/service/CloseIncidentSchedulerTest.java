package com.rtambun.scheduler.scheduler.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rtambun.scheduler.scheduler.data.dto.IncidentData;
import com.rtambun.scheduler.scheduler.model.CloseIncident;
import com.rtambun.scheduler.scheduler.service.time.IInstantProvider;
import com.rtambun.scheduler.scheduler.util.InstantGenerator;
import io.github.rtambun.dto.incident.Incident;
import io.github.rtambun.dto.incident.Status;
import io.github.rtambun.dto.kafka.IncidentKafka;
import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.JobDetails;
import org.jobrunr.jobs.JobParameter;
import org.jobrunr.jobs.lambdas.JobLambda;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.storage.JobStats;
import org.jobrunr.storage.PageRequest;
import org.jobrunr.storage.StorageProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

class CloseIncidentSchedulerTest {
    private JobScheduler mockJobScheduler;
    private IInstantProvider mockInstantProvider;
    private Jackson2ObjectMapperBuilder mockMapperBuilder;
    private StorageProvider mockStorageProvider;
    private long minutesAfterClose;
    private CloseIncidentScheduler closeIncidentScheduler;

    @BeforeEach
    void setUp() {
        mockJobScheduler = mock(JobScheduler.class);
        mockInstantProvider = mock(IInstantProvider.class);
        mockMapperBuilder = mock(Jackson2ObjectMapperBuilder.class);
        mockStorageProvider = mock(StorageProvider.class);
        minutesAfterClose = 5;
        closeIncidentScheduler = new CloseIncidentScheduler(mockJobScheduler,
                mockStorageProvider,
                mockInstantProvider,
                mockMapperBuilder,
                minutesAfterClose);
    }

    @Test
    void scheduleRemoveCloseIncident_EmptyCloseIncident() {

        Incident incident = new Incident();
        incident.setStatus(Status.CLOSED);

        Instant now = InstantGenerator.generateInstantUTC(2022, 4, 25, 17, 35, 6);
        when(mockInstantProvider.now()).thenReturn(now);
        when(mockMapperBuilder.build()).thenReturn(new ObjectMapper().registerModule(new JavaTimeModule()));

        closeIncidentScheduler.scheduleRemoveCloseIncident(incident);

        verify(mockInstantProvider, times(1)).now();
        verify(mockJobScheduler, times(0)).schedule(any(Instant.class), any(JobLambda.class));
        //verify(mockCloseIncidentKafka, times(0)).sendRemoveCloseIncidentMessage(any(MqttPayload.class));
    }

    @Test
    void scheduleRemoveCloseIncident_EmptyNotCloseIncident() {

        Incident incident = new Incident();
        incident.setStatus(Status.OPEN);

        Instant now = InstantGenerator.generateInstantUTC(2022, 4, 25, 17, 35, 6);
        when(mockInstantProvider.now()).thenReturn(now);
        when(mockMapperBuilder.build()).thenReturn(new ObjectMapper().registerModule(new JavaTimeModule()));
        when(mockStorageProvider.getJobStats()).thenReturn(new JobStats(Instant.now(),
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0,
                0));
        when(mockStorageProvider.getScheduledJobs(any(), any())).thenReturn(new ArrayList<>());

        closeIncidentScheduler.scheduleRemoveCloseIncident(incident);

        verify(mockInstantProvider, times(1)).now();
        verify(mockStorageProvider, times(1)).getJobStats();

        ArgumentCaptor<Instant> instantArgumentCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<PageRequest> pageRequestArgumentCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(mockStorageProvider, times(1)).getScheduledJobs(instantArgumentCaptor.capture(),
                pageRequestArgumentCaptor.capture());
        assertThat(instantArgumentCaptor.getValue()).isEqualTo(now.minusSeconds(-60*minutesAfterClose));
        assertThat(pageRequestArgumentCaptor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(new PageRequest("updatedAt:" + PageRequest.Order.DESC.name(), 0, 0));

        verify(mockJobScheduler, times(0)).schedule(any(Instant.class), any(JobLambda.class));
        //verify(mockCloseIncidentKafka, times(0)).sendRemoveCloseIncidentMessage(any(MqttPayload.class));
    }

    @Test
    void scheduleRemoveCloseIncident_CloseDateIsMoreThanNowMinusMinutesAfterClose() {
        Instant now = InstantGenerator.generateInstantUTC(2022, 4, 25, 17, 35, 6);

        Incident incident = new Incident();
        incident.setStatus(Status.CLOSED);
        incident.setCloseDate(now.minusSeconds(minutesAfterClose * 60 + 1));

        when(mockMapperBuilder.build()).thenReturn(new ObjectMapper().registerModule(new JavaTimeModule()));
        when(mockInstantProvider.now()).thenReturn(now);

        closeIncidentScheduler.scheduleRemoveCloseIncident(incident);

        verify(mockInstantProvider, times(1)).now();
        verify(mockMapperBuilder, times(2)).build();
        verify(mockJobScheduler, times(0)).schedule(any(Instant.class), any(JobLambda.class));
        //verify(mockCloseIncidentKafka, times(0)).sendRemoveCloseIncidentMessage(any(MqttPayload.class));
    }

    @Test
    void scheduleRemoveCloseIncident_CloseDateIsExactlyAtNowMinusMinutesAfterClose() {
        Instant now = InstantGenerator.generateInstantUTC(2022, 4, 25, 17, 35, 6);

        Incident incident = new Incident();
        incident.setStatus(Status.CLOSED);
        incident.setCloseDate(now.minusSeconds(minutesAfterClose * 60));

        when(mockMapperBuilder.build()).thenReturn(new ObjectMapper().registerModule(new JavaTimeModule()));
        when(mockInstantProvider.now()).thenReturn(now);

        closeIncidentScheduler.scheduleRemoveCloseIncident(incident);

        verify(mockInstantProvider, times(1)).now();
        verify(mockMapperBuilder, times(2)).build();
        verify(mockJobScheduler, times(0)).schedule(any(Instant.class), any(JobLambda.class));
        //verify(mockCloseIncidentKafka, times(0)).sendRemoveCloseIncidentMessage(any(MqttPayload.class));
    }

    @Test
    void scheduleRemoveCloseIncident_CloseDateIsAfterThanNowMinusMinutesAfterClose() throws Exception {
        Instant now = InstantGenerator.generateInstantUTC(2022, 4, 25, 17, 35, 6);

        Incident incident = new Incident();
        incident.setStatus(Status.CLOSED);
        incident.setCloseDate(now.minusSeconds(minutesAfterClose * 60 - 1));

        when(mockMapperBuilder.build()).thenReturn(new ObjectMapper().registerModule(new JavaTimeModule()));
        when(mockInstantProvider.now()).thenReturn(now);

        closeIncidentScheduler.scheduleRemoveCloseIncident(incident);

        verify(mockInstantProvider, times(1)).now();
        verify(mockMapperBuilder, times(2)).build();
        ArgumentCaptor<Instant> instantArgumentCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<JobLambda> jobLambdaArgumentCaptor = ArgumentCaptor.forClass(JobLambda.class);
        verify(mockJobScheduler, times(1)).schedule(instantArgumentCaptor.capture(),
                jobLambdaArgumentCaptor.capture());
        assertThat(instantArgumentCaptor.getValue())
                .isEqualTo(incident.getCloseDate().minusSeconds(minutesAfterClose * -60));
        JobLambda jobLambda = jobLambdaArgumentCaptor.getValue();
        jobLambda.run();
        ArgumentCaptor<IncidentKafka> incidentKafkaArgumentCaptor = ArgumentCaptor.forClass(IncidentKafka.class);
        //verify(mockCloseIncidentKafka, times(1))
        //        .sendRemoveCloseIncidentMessage(incidentKafkaArgumentCaptor.capture());
        IncidentKafka expected = new IncidentKafka();
        expected.setPayloadTypeCategory("RemovedBlueIndicatorFromMap");
        expected.setPayloadType("Incident");
        Incident expectedIncident = new Incident();
        expectedIncident.setCloseDate(now.minusSeconds(minutesAfterClose * 60 - 1));
        expectedIncident.setStatus(Status.CLOSED);
        expected.setPayload(new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(expectedIncident));
        assertThat(incidentKafkaArgumentCaptor.getValue()).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void listenTopic_MqttPayloadError_DoNothing() throws JsonProcessingException {
        String jsonPayload = "{\"key\":\"value\"}";
        ObjectNode objectNode = (ObjectNode) new ObjectMapper().readTree(jsonPayload);

        when(mockMapperBuilder.build()).thenReturn(new ObjectMapper().registerModule(new JavaTimeModule()));

        closeIncidentScheduler.listenTopic(objectNode);

        verify(mockMapperBuilder, times(1)).build();
        verify(mockInstantProvider, times(0)).now();
        verify(mockJobScheduler, times(0)).schedule(any(Instant.class), any(JobLambda.class));
    }

    @ParameterizedTest
    @MethodSource(value = "getData_listenTopic_MqttPayloadNotIncidentAndIncidentStatusUpdated_DoNothing")
    void listenTopic_MqttPayloadNotIncidentAndIncidentStatusUpdated_DoNothing(String payloadType,
                                                                              String payloadTypeCategory) throws JsonProcessingException {
        String jsonPayload = "{\"Payload\":\"\"," +
                "\"PayloadType\":" + payloadType + "," +
                "\"PayloadTypeCategory\":" + payloadTypeCategory + "}}";
        ObjectNode objectNode = (ObjectNode) new ObjectMapper().readTree(jsonPayload);

        when(mockMapperBuilder.build()).thenReturn(new ObjectMapper().registerModule(new JavaTimeModule()));

        closeIncidentScheduler.listenTopic(objectNode);

        verify(mockMapperBuilder, times(1)).build();
        verify(mockInstantProvider, times(1)).now();
        verify(mockJobScheduler, times(0)).schedule(any(Instant.class), any(JobLambda.class));
    }

    static Stream<Arguments> getData_listenTopic_MqttPayloadNotIncidentAndIncidentStatusUpdated_DoNothing() {
        return Stream.of(Arguments.of(null, null),
                Arguments.of(null, "\"\""),
                Arguments.of("\"\"", null),
                Arguments.of("\"\"", "\"\""),
                Arguments.of(null, "\"IncidentStatusUpdated\""),
                Arguments.of("\"\"", "\"IncidentStatusUpdated\""),
                Arguments.of("\"any\"", "\"IncidentStatusUpdated\""),
                Arguments.of("\"Incident\"", null),
                Arguments.of("\"Incident\"", "\"\""),
                Arguments.of("\"Incident\"", "\"any\"")
        );
    }

    @ParameterizedTest
    @NullSource
    void listenTopic_MqttPayloadValueIsNull(String payload) throws JsonProcessingException {
        String jsonPayload = "{\"Payload\":" + payload + "," +
                "\"PayloadType\":\"Incident\"," +
                "\"PayloadTypeCategory\":\"IncidentStatusUpdated\"}";
        ObjectNode objectNode = (ObjectNode) new ObjectMapper().readTree(jsonPayload);

        when(mockMapperBuilder.build()).thenReturn(new ObjectMapper().registerModule(new JavaTimeModule()));

        closeIncidentScheduler.listenTopic(objectNode);

        verify(mockMapperBuilder, times(1)).build();
        verify(mockInstantProvider, times(1)).now();
        verify(mockJobScheduler, times(0)).schedule(any(Instant.class), any(JobLambda.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"\""})
    void listenTopic_MqttPayloadValueIsEmpty(String payload) throws JsonProcessingException {
        String jsonPayload = "{\"Payload\":" + payload + "," +
                "\"PayloadType\":\"Incident\"," +
                "\"PayloadTypeCategory\":\"IncidentStatusUpdated\"}";
        ObjectNode objectNode = (ObjectNode) new ObjectMapper().readTree(jsonPayload);

        when(mockMapperBuilder.build()).thenReturn(new ObjectMapper().registerModule(new JavaTimeModule()));

        closeIncidentScheduler.listenTopic(objectNode);

        verify(mockMapperBuilder, times(2)).build();
        verify(mockInstantProvider, times(1)).now();
        verify(mockJobScheduler, times(0)).schedule(any(Instant.class), any(JobLambda.class));
    }

    @Test
    void listenTopic_MqttPayload_IncidentIsClosed_WithinXMinutes() throws Exception {
        String closeDate = "28/04/2022 14:43:00";
        String payloadJson = IncidentData.IncidentData1.replace("\"closeDate\":\"27/04/2022 11:48:54\"",
                "\"closeDate\":\"28/04/2022 14:43:00\"");

        IncidentKafka incidentKafka = new IncidentKafka(payloadJson,
                "Incident",
                "IncidentStatusUpdated");

        String mqttJsonPayload = new ObjectMapper().writeValueAsString(incidentKafka);
        ObjectNode objectNode = (ObjectNode) new ObjectMapper().readTree(mqttJsonPayload);

        when(mockMapperBuilder.build()).thenReturn(new ObjectMapper().registerModule(new JavaTimeModule()));
        when(mockInstantProvider.now()).thenReturn(InstantGenerator.generateInstantUTC(2022, 4, 28, 14, 42, 0));

        closeIncidentScheduler.listenTopic(objectNode);

        verify(mockMapperBuilder, times(2)).build();
        verify(mockInstantProvider, times(1)).now();
        CloseIncident closeIncident = new CloseIncident(
                "7091125584690244490",
                "TU/20220427/0002",
                "3",
                "Alarm Button",
                InstantGenerator.generateInstantUTC(2022, 4, 28, 14, 43, 0));
        ArgumentCaptor<Instant> instantArgumentCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<JobLambda> jobLambdaArgumentCaptor = ArgumentCaptor.forClass(JobLambda.class);
        verify(mockJobScheduler, times(1)).schedule(instantArgumentCaptor.capture(),
                jobLambdaArgumentCaptor.capture());
        assertThat(instantArgumentCaptor.getValue())
                .isEqualTo(closeIncident.getCloseDate().minusSeconds(minutesAfterClose * -60));
        JobLambda jobLambda = jobLambdaArgumentCaptor.getValue();
        jobLambda.run();
        ArgumentCaptor<IncidentKafka> incidentKafkaArgumentCaptor = ArgumentCaptor.forClass(IncidentKafka.class);
        //verify(mockCloseIncidentKafka, times(1))
        //        .sendRemoveCloseIncidentMessage(incidentKafkaArgumentCaptor.capture());
        IncidentKafka expected = new IncidentKafka(payloadJson,
                "Incident",
                "RemovedBlueIndicatorFromMap");
        assertThat(incidentKafkaArgumentCaptor.getValue()).usingRecursiveComparison().isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(ints = {5,6})
    void listenTopic_MqttPayload_IncidentIsClosed_AfterXMinutes(int minutesAfterClose) throws JsonProcessingException {
        String payloadJson = IncidentData.IncidentData1.replace("\"closedDate\":\"27/04/2022 11:48:54\"",
                "\"closedDate\":\"28/04/2022 14:43:00\"");
        IncidentKafka incidentKafka = new IncidentKafka(payloadJson,
                "Incident",
                "IncidentStatusUpdated");

        String mqttJsonPayload = new ObjectMapper().writeValueAsString(incidentKafka);
        ObjectNode objectNode = (ObjectNode) new ObjectMapper().readTree(mqttJsonPayload);

        when(mockMapperBuilder.build()).thenReturn(new ObjectMapper().registerModule(new JavaTimeModule()));
        when(mockInstantProvider.now()).thenReturn(InstantGenerator.generateInstantUTC(2022, 4, 28, 14, 43+minutesAfterClose, 0));

        closeIncidentScheduler.listenTopic(objectNode);

        verify(mockMapperBuilder, times(2)).build();
        verify(mockInstantProvider, times(1)).now();
        verify(mockJobScheduler, times(0)).schedule(any(Instant.class), any(JobLambda.class));
    }

    @Test
    void listenTopic_MqttPayload_IncidentIsReOpened_WithinXMinutes() throws JsonProcessingException {
        String payloadJson = IncidentData.IncidentData1.replace("\"closeDate\":\"27/04/2022 11:48:54\"",
                        "\"closeDate\":\"28/04/2022 14:43:00\"")
                .replace("\"status\":{\"ID\":4,\"name\":\"Closed\"",
                        "\"status\":{\"ID\":4,\"name\":\"Open\"");

        IncidentKafka incidentKafka = new IncidentKafka(payloadJson,
                "Incident",
                "IncidentStatusUpdated");

        String mqttJsonPayload = new ObjectMapper().writeValueAsString(incidentKafka);
        ObjectNode objectNode = (ObjectNode) new ObjectMapper().readTree(mqttJsonPayload);

        when(mockMapperBuilder.build()).thenReturn(new ObjectMapper().registerModule(new JavaTimeModule()));
        Instant now = InstantGenerator.generateInstantUTC(2022,
                4,
                28,
                14,
                43 + 3,
                0);
        when(mockInstantProvider.now()).thenReturn(now);
        when(mockStorageProvider.getJobStats()).thenReturn(new JobStats(now,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0,
                0));

        String payloadJsonStored = IncidentData.IncidentData1.replace("\"closeDate\":\"27/04/2022 11:48:54\"",
                "\"closeDate\":\"28/04/2022 14:43:00\"");
        IncidentKafka incidentKafkaStored = new IncidentKafka(payloadJsonStored,
                "Incident",
                "IncidentStatusUpdated");
        UUID uuid = UUID.randomUUID();
        when(mockStorageProvider.getScheduledJobs(any(), any())).thenReturn(new ArrayList<>(
                List.of(new Job(uuid, new JobDetails("CloseIncidentKafka",
                        null,
                        "sendRemoveCloseIncidentMessage",
                        new ArrayList<>(List.of(new JobParameter(incidentKafkaStored))))
                ))
        ));

        closeIncidentScheduler.listenTopic(objectNode);

        verify(mockMapperBuilder, times(2)).build();
        verify(mockInstantProvider, times(1)).now();
        verify(mockJobScheduler, times(0)).schedule(any(Instant.class), any(JobLambda.class));
        ArgumentCaptor<UUID> uuidArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(mockStorageProvider, times(1)).deletePermanently(uuidArgumentCaptor.capture());
        assertThat(uuidArgumentCaptor.getValue()).usingRecursiveComparison().isEqualTo(uuid);
    }

    @Test
    void listenTopic_MqttPayload_IncidentIsReOpened_AfterXMinutes() throws JsonProcessingException {
        String payloadJson = IncidentData.IncidentData1.replace("\"closedDate\":\"27/04/2022 11:48:54\"",
                        "\"closedDate\":\"28/04/2022 14:43:00\"")
                .replace("\"status\":{\"ID\":4,\"name\":\"Closed\"",
                        "\"status\":{\"ID\":4,\"name\":\"Open\"");

        IncidentKafka incidentKafka = new IncidentKafka(payloadJson,
                "Incident",
                "IncidentStatusUpdated");

        String mqttJsonPayload = new ObjectMapper().writeValueAsString(incidentKafka);
        ObjectNode objectNode = (ObjectNode) new ObjectMapper().readTree(mqttJsonPayload);

        when(mockMapperBuilder.build()).thenReturn(new ObjectMapper().registerModule(new JavaTimeModule()));
        Instant now = InstantGenerator.generateInstantUTC(2022,
                4,
                28,
                14,
                43 + 3,
                0);
        when(mockInstantProvider.now()).thenReturn(now);
        when(mockStorageProvider.getJobStats()).thenReturn(new JobStats(now,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0,
                0));

        String payloadJsonStored = IncidentData.IncidentData1.replace("\"closedDate\":\"27/04/2022 11:48:54\"",
                "\"closedDate\":\"28/04/2022 14:43:00\"");
        IncidentKafka incidentKafkaStored = new IncidentKafka(payloadJsonStored,
                "Incident",
                "IncidentStatusUpdated");

        UUID expectedUuid = UUID.randomUUID();
        List<Job> storedJob = List.of(new Job(expectedUuid,
                new JobDetails("CloseIncidentKafka", null, "sendRemoveCloseIncidentMessage",
                        new ArrayList<>(List.of(new JobParameter(incidentKafkaStored))))
        ));

        when(mockStorageProvider.getScheduledJobs(any(), any())).thenReturn(storedJob);

        closeIncidentScheduler.listenTopic(objectNode);

        verify(mockMapperBuilder, times(2)).build();
        verify(mockInstantProvider, times(1)).now();
        verify(mockJobScheduler, times(0)).schedule(any(Instant.class), any(JobLambda.class));
        ArgumentCaptor<UUID> uuidArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(mockStorageProvider, times(1)).deletePermanently(uuidArgumentCaptor.capture());
        assertThat(uuidArgumentCaptor.getValue()).usingRecursiveComparison().isEqualTo(expectedUuid);
    }

    @ParameterizedTest
    @NullSource
    @MethodSource(value = "getData_listenTopic_MqttPayload_IncidentIsReOpened_NoOngoingJobOrJobIsDifferentLabel")
    void listenTopic_MqttPayload_IncidentIsReOpened_NoOngoingJobOrJobIsDifferentLabel(List<Job> storedJob)
            throws JsonProcessingException {
        String payloadJson = IncidentData.IncidentData1.replace("\"closeDate\":\"27/04/2022 11:48:54\"",
                        "\"closeDate\":\"28/04/2022 14:43:00\"")
                .replace("\"status\":{\"ID\":4,\"name\":\"Closed\"",
                        "\"status\":{\"ID\":4,\"name\":\"Open\"");

        IncidentKafka incidentKafka = new IncidentKafka(payloadJson,
                "Incident",
                "IncidentStatusUpdated");

        String mqttJsonPayload = new ObjectMapper().writeValueAsString(incidentKafka);
        ObjectNode objectNode = (ObjectNode) new ObjectMapper().readTree(mqttJsonPayload);

        when(mockMapperBuilder.build()).thenReturn(new ObjectMapper().registerModule(new JavaTimeModule()));
        Instant now = InstantGenerator.generateInstantUTC(2022,
                4,
                28,
                14,
                43 + 3,
                0);
        when(mockInstantProvider.now()).thenReturn(now);
        when(mockStorageProvider.getJobStats()).thenReturn(new JobStats(now,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0,
                0));

        when(mockStorageProvider.getScheduledJobs(any(), any())).thenReturn(storedJob);

        closeIncidentScheduler.listenTopic(objectNode);

        verify(mockMapperBuilder, times(2)).build();
        verify(mockInstantProvider, times(1)).now();
        verify(mockJobScheduler, times(0)).schedule(any(Instant.class), any(JobLambda.class));
        verify(mockStorageProvider, times(0)).deletePermanently(any());
    }

    static Stream<Arguments> getData_listenTopic_MqttPayload_IncidentIsReOpened_NoOngoingJobOrJobIsDifferentLabel() {
        String payloadJsonStored = IncidentData.IncidentData1.replace("\"closedDate\":\"27/04/2022 11:48:54\"",
                        "\"closedDate\":\"28/04/2022 14:43:00\"")
                .replace("\"label\":\"TU/20220427/0002\"", "\"label\":\"TU/20220427/0001\"");
        IncidentKafka incidentKafkaStored = new IncidentKafka(payloadJsonStored,
                "Incident",
                "IncidentStatusUpdated");

        return Stream.of(Arguments.of(new ArrayList<Job>()),
                Arguments.of(List.of(new Job(null,
                        new JobDetails("CloseIncidentKafka", null, "sendRemoveCloseIncidentMessage",
                                new ArrayList<>(List.of(new JobParameter(incidentKafkaStored))))
                ))));
    }

}