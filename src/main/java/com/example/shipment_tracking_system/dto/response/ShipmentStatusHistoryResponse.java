package com.example.shipment_tracking_system.dto.response;

import com.example.shipment_tracking_system.model.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentStatusHistoryResponse {
    private ShipmentStatus status;
    private String note;
    private Instant changedAt;
    private String changedBy;
}