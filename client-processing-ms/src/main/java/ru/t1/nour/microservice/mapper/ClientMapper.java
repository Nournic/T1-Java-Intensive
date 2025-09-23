package ru.t1.nour.microservice.mapper;

import org.mapstruct.*;
import ru.t1.nour.microservice.model.Client;
import ru.t1.nour.microservice.model.dto.ClientDto;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClientMapper {
    Client toEntity(ClientDto clientDto);

    ClientDto toDto(Client client);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Client partialUpdate(ClientDto clientDto, @MappingTarget Client client);
}
