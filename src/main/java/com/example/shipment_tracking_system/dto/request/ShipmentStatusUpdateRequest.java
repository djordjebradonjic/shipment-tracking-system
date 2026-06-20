package com.example.shipment_tracking_system.dto.request;

import com.example.shipment_tracking_system.model.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentStatusUpdateRequest {

    @NotNull
    private ShipmentStatus status;

    @Size(max = 500)
    private String note;
}