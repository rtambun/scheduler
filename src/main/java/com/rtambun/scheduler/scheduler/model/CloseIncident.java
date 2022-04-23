package com.rtambun.scheduler.scheduler.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.Instant;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "close_incident")
public class CloseIncident {

    @Id
    private String id;
    private String type;
    private String severity;
    private Instant closedDate;

}
