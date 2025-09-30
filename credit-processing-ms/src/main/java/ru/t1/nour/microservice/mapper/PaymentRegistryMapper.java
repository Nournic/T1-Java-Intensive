package ru.t1.nour.microservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.t1.nour.microservice.model.PaymentRegistry;
import ru.t1.nour.microservice.model.dto.NextCreditPaymentDTO;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentRegistryMapper {
    NextCreditPaymentDTO toNextCreditPaymentDTO(PaymentRegistry paymentRegistry);
}