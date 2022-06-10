package io.github.rtambun.scheduler.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.rtambun.scheduler.service.time.IInstantProvider;
import io.github.rtambun.dto.incident.Incident;
import io.github.rtambun.dto.incident.Status;
import io.github.rtambun.dto.kafka.IncidentKafka;
import lombok.extern.log4j.Log4j2;
import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.JobDetails;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.storage.JobStats;
import org.jobrunr.storage.PageRequest;
import org.jobrunr.storage.StorageProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Log4j2
public class CloseIncidentScheduler {

    private final JobScheduler jobScheduler;
    private final StorageProvider storageProvider;
    private final IInstantProvider instantProvider;
    private final Jackson2ObjectMapperBuilder mapperBuilder;
    private final CloseIncidentKafka closeIncidentKafka;
    private final long minutesAfterClose;

    public CloseIncidentScheduler(JobScheduler jobScheduler,
                                  StorageProvider storageProvider,
                                  IInstantProvider instantProvider,
                                  Jackson2ObjectMapperBuilder mapperBuilder,
                                  CloseIncidentKafka closeIncidentKafka,
                                  @Value("${incident.close.minutes.after}") long minutesAfterClose) {
        this.jobScheduler = jobScheduler;
        this.storageProvider = storageProvider;
        this.instantProvider = instantProvider;
        this.mapperBuilder = mapperBuilder;
        this.closeIncidentKafka = closeIncidentKafka;
        this.minutesAfterClose = minutesAfterClose;
    }

    public void scheduleRemoveCloseIncident(Incident incident) {
        if (incident.getCloseDate() == null &&
                (incident.getStatus() != null &&
                incident.getStatus() == Status.CLOSED)
        ) {
            log.info("Closed date is null, set closed date to Instant.MIN");
            incident.setCloseDate(Instant.MIN);
        }

        Instant now = instantProvider.now();
        log.info("Scheduling incident {} which is closed at {}",
                incident.getLabel(),
                incident.getCloseDate());
        log.info("Current time {}", now);

        ObjectMapper objectMapper = mapperBuilder.build();

        try {
            IncidentKafka mqttPayload = new IncidentKafka();
            mqttPayload.setPayload(objectMapper.writeValueAsString(incident));
            mqttPayload.setPayloadType(IncidentKafka.INCIDENT_PAYLOAD_TYPE_INCIDENT);
            mqttPayload.setPayloadTypeCategory(IncidentKafka.INCIDENT_PAYLOAD_CATEGORY_INCIDENT_STATUS_UPDATE);

            scheduleJob(mqttPayload, now);
        }catch (JsonProcessingException jpe) {
            log.error("Error processing object, {}", jpe.getMessage());
        }
    }

    @KafkaListener(topics = "${isms.kafka.topic}", groupId = "${kafka.client.id}")
    public void listenTopic(ObjectNode objectNode) {

        log.info("Message is {}", objectNode);

        String payload = objectNode.toString();

        try {
            ObjectMapper objectMapper = mapperBuilder.build();
            IncidentKafka incidentKafka = objectMapper.readValue(payload, new TypeReference<>() {});
            scheduleJob(incidentKafka, instantProvider.now());


        } catch (JsonProcessingException jpe) {
            log.error("Error processing message, {}", jpe.getMessage());
        }
    }

    private void scheduleJob(IncidentKafka mqttPayload, Instant now) throws JsonProcessingException {
        if (mqttPayload.getPayloadType() == null || mqttPayload.getPayloadTypeCategory() == null) {
            log.error("Either PayloadType or PayloadTypeCategory is null");
            return;
        }

        if (mqttPayload
                .getPayloadType()
                .equalsIgnoreCase(IncidentKafka.INCIDENT_PAYLOAD_TYPE_INCIDENT) &&
                mqttPayload
                        .getPayloadTypeCategory()
                        .equalsIgnoreCase(IncidentKafka.INCIDENT_PAYLOAD_CATEGORY_INCIDENT_STATUS_UPDATE)) {
            if (mqttPayload.getPayload() == null) {
                log.error("Payload is null");
                return;
            }
            ObjectMapper objectMapper = mapperBuilder.build();
            Incident incident = objectMapper.readValue(mqttPayload.getPayload(), new TypeReference<>() {});
            if (incident.getStatus() == Status.CLOSED) {
                log.info("Incident {} is closed, schedule sending message",
                        incident.getLabel());
                mqttPayload.setPayloadTypeCategory(
                        IncidentKafka.INCIDENT_PAYLOAD_CATEGORY_INCIDENT_REMOVE_BLUE_INDICATOR_FROM_MAP);
                Instant showingEndData = incident.getCloseDate().minusSeconds(-60 * minutesAfterClose);
                String messageInfo;
                if (showingEndData.isAfter(now)) {
                    messageInfo = "Incident {} showing end date {} is after current time, job is scheduled";
                    jobScheduler.schedule(showingEndData, () -> closeIncidentKafka.sendRemoveCloseIncidentMessage(
                            mqttPayload));
                } else {
                    messageInfo = "Incident {} showing end date {} is before current time, job is not scheduled";
                }
                log.info(messageInfo, incident.getLabel(), showingEndData);
            } else if (incident.getStatus() == Status.OPEN){
                log.info("Incident {} is open, check if the incident is already scheduled",
                        incident.getLabel());
                JobStats jobStats = storageProvider.getJobStats();
                PageRequest pageRequest = PageRequest.descOnUpdatedAt(jobStats.getScheduled().intValue());
                List<Job> jobs = storageProvider.getScheduledJobs(now.minusSeconds(-60*minutesAfterClose), pageRequest);
                if (jobs == null) {
                    log.info("There is no stored jobs. Ignore incident status update to open");
                    return;
                }
                for (Job job : jobs) {
                    JobDetails jobDetails = job.getJobDetails();
                    Class[] parameterTypes = jobDetails.getJobParameterTypes();
                    if (parameterTypes.length == 1 && parameterTypes[0] == IncidentKafka.class) {
                        IncidentKafka storedIncidentKafka = (IncidentKafka) jobDetails.getJobParameterValues()[0];
                        Incident storedIncident = objectMapper.readValue(storedIncidentKafka.getPayload()
                                , new TypeReference<>() {});
                        if (storedIncident.getLabel().equalsIgnoreCase(incident.getLabel())) {
                            storageProvider.deletePermanently(job.getId());
                        }
                    }
                }
            } else {
                log.info("Incident {} is {} state, do nothing",
                        incident.getLabel(), incident.getStatus());
            }
        }
    }
}