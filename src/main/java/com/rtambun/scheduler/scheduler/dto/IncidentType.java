package com.rtambun.scheduler.scheduler.dto;

import lombok.*;

import java.io.Serializable;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IncidentType implements Serializable {
    private String type;
    private String severity;
}
