package com.example.shipment_tracking_system.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentCreateRequest {


    private Long userId;

    @NotBlank
    @Size(max = 500)
    private String description;

    @NotBlank
    @Size(max = 200)
    private String origin;

    @NotBlank
    @Size(max = 200)
    private String destination;

    @DecimalMin(value = "0.01")
    private BigDecimal weightKg;
}