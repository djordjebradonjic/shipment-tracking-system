package com.example.shipment_tracking_system.service;

import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.UUID;

@Component
public class TrackingNumberGenerator {

    public String generate() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "#" + Year.now().getValue() + "-" + suffix;
    }
}
