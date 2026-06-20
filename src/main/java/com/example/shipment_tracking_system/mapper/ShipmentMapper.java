package com.example.shipment_tracking_system.mapper;

import com.example.shipment_tracking_system.dto.request.ShipmentCreateRequest;
import com.example.shipment_tracking_system.dto.response.ShipmentResponse;
import com.example.shipment_tracking_system.model.Shipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.shipment_tracking_system.dto.response.ShipmentStatusHistoryResponse;
import com.example.shipment_tracking_system.model.ShipmentStatusHistory;

@Mapper(componentModel = "spring")
public interface ShipmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trackingNumber", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "currentStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Shipment toEntity(ShipmentCreateRequest request);

    @Mapping(source = "user.id", target = "userId")
    ShipmentResponse toResponse(Shipment shipment);

    ShipmentStatusHistoryResponse toHistoryResponse(ShipmentStatusHistory history);
}