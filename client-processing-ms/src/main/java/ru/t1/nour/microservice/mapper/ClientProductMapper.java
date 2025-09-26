package ru.t1.nour.microservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.t1.nour.microservice.model.ClientProduct;
import ru.t1.nour.microservice.model.dto.response.ClientProductResponse;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClientProductMapper {
    ClientProduct toEntity(ClientProductResponse clientProductResponse);

    ClientProductResponse toClientProductResponse(ClientProduct clientProduct);
}