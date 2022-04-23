package com.rtambun.scheduler.scheduler.repository;

import com.rtambun.scheduler.scheduler.model.CloseIncident;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.time.Instant;
import java.util.List;

public interface CloseIncidentRepository extends ElasticsearchRepository<CloseIncident, String> {

    List<CloseIncident> findByClosedDateAfter (Instant date);
}
