package ru.t1.nour.microservice.mapper;

import org.mapstruct.*;
import ru.t1.nour.microservice.model.Client;
import ru.t1.nour.microservice.model.dto.response.ClientResponse;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClientMapper {
    Client toEntity(ClientResponse clientResponse);

    ClientResponse toDto(Client client);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Client partialUpdate(ClientResponse clientResponse, @MappingTarget Client client);
}
