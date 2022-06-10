package io.github.rtambun.scheduler.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.rtambun.scheduler.data.dto.IncidentData;
import io.github.rtambun.scheduler.model.CloseIncident;
import io.github.rtambun.scheduler.util.InstantGenerator;
import io.github.rtambun.dto.kafka.IncidentKafka;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.ArgumentCaptor;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

class CloseIncidentKafkaTest {

    private KafkaTemplate<String, Object> mockKafkaTemplate;
    private Jackson2ObjectMapperBuilder mockMapperBuilder;

    private CloseIncidentKafka closeIncidentKafka;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        mockKafkaTemplate = mock(KafkaTemplate.class);
        mockMapperBuilder = mock(Jackson2ObjectMapperBuilder.class);
        String kafkaTopicInc = "inc";

        closeIncidentKafka = new CloseIncidentKafka(mockKafkaTemplate, mockMapperBuilder, kafkaTopicInc);
    }

    @Test
    void sendRemoveCloseIncidentMessage() throws JsonProcessingException {

        Instant now = InstantGenerator.generateInstantUTC(2022, 5,22, 8, 0,0);

        CloseIncident expected = new CloseIncident(null,
                "name",
                "severity",
                "type", now);

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        when(mockMapperBuilder.build()).thenReturn(objectMapper);
        closeIncidentKafka.sendRemoveCloseIncidentMessage(expected);

        ArgumentCaptor<ObjectNode> objectNodeArgumentCaptor = ArgumentCaptor.forClass(ObjectNode.class);
        verify(mockKafkaTemplate, times(1)).send(eq("inc"),
                objectNodeArgumentCaptor.capture());

        CloseIncident actual = objectMapper.readValue(objectNodeArgumentCaptor.getValue().toString(),
                CloseIncident.class);

        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields()
                .isEqualTo(expected);
    }

    @Test
    void sendRemoveCloseIncidentMessage_MapperThrowException_DoNothing() throws JsonProcessingException {

        Instant now = InstantGenerator.generateInstantUTC(2022, 5,22, 8, 0,0);

        CloseIncident expected = new CloseIncident(null,
                "name",
                "severity",
                "type", now);

        ObjectMapper objectMapper = mock(ObjectMapper.class);
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("test behavior"){});
        when(mockMapperBuilder.build()).thenReturn(objectMapper);

        closeIncidentKafka.sendRemoveCloseIncidentMessage(expected);

        verify(mockKafkaTemplate, times(0)).send(any(), any());
    }

    @Test
    void sendRemoveCloseIncidentMessage_KafkaPayload() throws JsonProcessingException {
        IncidentKafka expected = new IncidentKafka(
                IncidentData.IncidentData1,
                "Incident",
                "IncidentStatusUpdated");

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        when(mockMapperBuilder.build()).thenReturn(objectMapper);

        closeIncidentKafka.sendRemoveCloseIncidentMessage(expected);

        ArgumentCaptor<ObjectNode> objectNodeArgumentCaptor = ArgumentCaptor.forClass(ObjectNode.class);
        verify(mockKafkaTemplate, times(1)).send(eq("inc"),
                objectNodeArgumentCaptor.capture());

        IncidentKafka actual = objectMapper.readValue(objectNodeArgumentCaptor.getValue().toString(),
                IncidentKafka.class);

        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @ParameterizedTest
    @NullSource
    @MethodSource(value = "getData_sendRemoveCloseIncidentMessage_MqttPayload_FormatWrong")
    void sendRemoveCloseIncidentMessage_KafkaPayload_FormatWrong(String payload) {
        IncidentKafka expected = new IncidentKafka(
                payload,
                "Incident",
                "IncidentStatusUpdated");

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        when(mockMapperBuilder.build()).thenReturn(objectMapper);

        closeIncidentKafka.sendRemoveCloseIncidentMessage(expected);

        verify(mockKafkaTemplate, times(0)).send(any(), any());
    }

    static Stream<Arguments> getData_sendRemoveCloseIncidentMessage_MqttPayload_FormatWrong() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of(" "),
                Arguments.of("\"key\":\"value\""),
                Arguments.of(IncidentData.IncidentData1.replace("\"label\":\"incident_label\"",
                        "\"label\":\"\"")),
                Arguments.of(IncidentData.IncidentData1.replace("\"label\":\"incident_label\"",
                        "\"label\":null"))
                );
    }
}