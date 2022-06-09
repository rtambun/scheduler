package com.rtambun.scheduler.scheduler.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

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
    @Field(type = FieldType.Text)
    private String name;
    @Field(type = FieldType.Text)
    private String severity;
    @Field(type = FieldType.Text)
    private String type;
    @Field(type = FieldType.Date_Nanos)
    private Instant closeDate;

}
