package ru.t1.nour.microservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.t1.nour.microservice.model.Product;
import ru.t1.nour.microservice.model.dto.request.ProductCreateRequest;
import ru.t1.nour.microservice.model.dto.response.ProductResponse;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {ClientMapper.class, ProductMapper.class})
public interface ProductMapper {
    Product toEntity(ProductResponse productResponse);

    ProductResponse toProductResponse(Product product);

    Product toEntity(ProductCreateRequest request);
}