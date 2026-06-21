package com.example.shipment_tracking_system.dto.response;

import com.example.shipment_tracking_system.model.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentResponse {

    private Long id;
    private String trackingNumber;
    private Long userId;
    private String description;
    private String origin;
    private String destination;
    private BigDecimal weightKg;
    private ShipmentStatus currentStatus;
    private Instant createdAt;
    private Instant updatedAt;
    private String ownerFullName;
    private String ownerEmail;
}