package io.github.rtambun.scheduler.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.rtambun.scheduler.model.CloseIncident;
import io.github.rtambun.dto.incident.Incident;
import io.github.rtambun.dto.kafka.IncidentKafka;
import lombok.extern.log4j.Log4j2;
import org.jobrunr.jobs.annotations.Job;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class CloseIncidentKafka {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Jackson2ObjectMapperBuilder mapperBuilder;
    private final String kafkaTopicInc;

    public CloseIncidentKafka(KafkaTemplate<String, Object> kafkaTemplate,
                              Jackson2ObjectMapperBuilder mapperBuilder,
                              @Value("${incident.kafka.topic}") String kafkaTopicInc) {
        this.kafkaTemplate = kafkaTemplate;
        this.mapperBuilder = mapperBuilder;
        this.kafkaTopicInc = kafkaTopicInc;
    }

    @Job
    public void sendRemoveCloseIncidentMessage(CloseIncident closeIncident) {
        log.info("Sending inc message to remove blue indicator from map for incident {}",
                closeIncident.getName());

        try {
            ObjectMapper objectMapper = mapperBuilder.build();
            String jsonString = objectMapper.writeValueAsString(closeIncident);
            ObjectNode objectNode = (ObjectNode) objectMapper.readTree(jsonString);
            kafkaTemplate.send(kafkaTopicInc, objectNode);
        } catch (JsonProcessingException jpe) {
            log.error("Failed to process closeIncident object, reason {}", jpe.getMessage());
        }
    }

    @Job
    public void sendRemoveCloseIncidentMessage(IncidentKafka incidentKafka) {
        try {
            if (incidentKafka.getPayload() == null || incidentKafka.getPayload().isBlank()) {
                log.error("Mqtt payload is not valid object, payload cant be empty or null");
                return;
            }

            ObjectMapper objectMapper = mapperBuilder.build();

            Incident incident = objectMapper.readValue(incidentKafka.getPayload(),
                    Incident.class);

            if (incident == null || incident.getLabel() == null || incident.getLabel().isEmpty()) {
                log.error("Mqtt payload is not valid Incident object, " +
                        "either payload cant be converted to Incident or label is empty");
                return;
            }

            log.info("Sending inc message to remove blue indicator from map for incident {}",
                    incident.getLabel());

            String jsonString = objectMapper.writeValueAsString(incidentKafka);
            ObjectNode objectNode = (ObjectNode) objectMapper.readTree(jsonString);
            kafkaTemplate.send(kafkaTopicInc, objectNode);
        } catch (JsonProcessingException jpe) {
            log.error("Failed to process closeIncident object, reason {}", jpe.getMessage());
        }
    }
}
