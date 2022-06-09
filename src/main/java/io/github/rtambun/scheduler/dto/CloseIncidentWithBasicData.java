package io.github.rtambun.scheduler.dto;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CloseIncidentWithBasicData implements Serializable {
    private String name;
    private IncidentType incidentType;
    private Date closeDate;
}
