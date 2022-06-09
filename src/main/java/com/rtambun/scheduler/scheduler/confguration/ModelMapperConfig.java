package com.rtambun.scheduler.scheduler.confguration;

import com.rtambun.scheduler.scheduler.model.CloseIncident;
import io.github.rtambun.dto.incident.Incident;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
@Log4j2
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        TypeMap<Incident, CloseIncident> typeMap = modelMapper.createTypeMap(Incident.class,
                CloseIncident.class);
        typeMap.addMappings(
                mapping -> mapping.map(Incident::getLabel, CloseIncident::setName));
        typeMap.addMappings(
                mapping -> mapping.map(Incident::getCloseDate, CloseIncident::setCloseDate));

        return modelMapper;
    }

    @Bean
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
        return new Jackson2ObjectMapperBuilder();
    }

}