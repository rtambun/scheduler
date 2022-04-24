package com.rtambun.scheduler.scheduler.dto;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CloseIncidentWithBasicDataResponse implements Serializable {
    private int count;
    private List<CloseIncidentWithBasicData> incidents;
}
