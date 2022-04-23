package com.rtambun.scheduler.scheduler.repository;

import com.rtambun.scheduler.scheduler.model.CloseIncident;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface CloseIncidentRepository extends ElasticsearchRepository<CloseIncident, String> {

    List<CloseIncident> findByClosedDateAfter (Instant date);
}
