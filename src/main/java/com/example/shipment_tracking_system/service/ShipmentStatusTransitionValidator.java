package com.example.shipment_tracking_system.service;

import com.example.shipment_tracking_system.model.ShipmentStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;


    @Component
    public class ShipmentStatusTransitionValidator {

        private static final Map<ShipmentStatus, Set<ShipmentStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(ShipmentStatus.class);

        static {
            ALLOWED_TRANSITIONS.put(ShipmentStatus.CREATED, EnumSet.of(ShipmentStatus.IN_TRANSIT, ShipmentStatus.CANCELLED));
            ALLOWED_TRANSITIONS.put(ShipmentStatus.IN_TRANSIT, EnumSet.of(ShipmentStatus.DELIVERED, ShipmentStatus.CANCELLED));
            ALLOWED_TRANSITIONS.put(ShipmentStatus.DELIVERED, EnumSet.noneOf(ShipmentStatus.class));
            ALLOWED_TRANSITIONS.put(ShipmentStatus.CANCELLED, EnumSet.noneOf(ShipmentStatus.class));
        }

        public boolean isAllowed(ShipmentStatus from, ShipmentStatus to) {
            return ALLOWED_TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
        }
    }

