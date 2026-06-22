package com.example.shipment_tracking_system.service;

import com.example.shipment_tracking_system.model.ShipmentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ShipmentStatusTransitionValidatorTest {

    private final ShipmentStatusTransitionValidator validator = new ShipmentStatusTransitionValidator();

    @ParameterizedTest(name = "{0} -> {1} should be ALLOWED")
    @CsvSource({
            "CREATED, IN_TRANSIT",
            "CREATED, CANCELLED",
            "IN_TRANSIT, DELIVERED",
            "IN_TRANSIT, CANCELLED"
    })
    void allowsValidTransitions(ShipmentStatus from, ShipmentStatus to) {
        assertThat(validator.isAllowed(from, to)).isTrue();
    }

    @ParameterizedTest(name = "{0} -> {1} should be REJECTED")
    @CsvSource({
            "CREATED, DELIVERED",
            "CREATED, CREATED",
            "IN_TRANSIT, CREATED",
            "IN_TRANSIT, IN_TRANSIT",
            "DELIVERED, CREATED",
            "DELIVERED, IN_TRANSIT",
            "DELIVERED, CANCELLED",
            "DELIVERED, DELIVERED",
            "CANCELLED, CREATED",
            "CANCELLED, IN_TRANSIT",
            "CANCELLED, DELIVERED",
            "CANCELLED, CANCELLED"
    })
    void rejectsInvalidTransitions(ShipmentStatus from, ShipmentStatus to) {
        assertThat(validator.isAllowed(from, to)).isFalse();
    }

    @Test
    void terminalStatusDeliveredCannotTransitionToAnything() {
        for (ShipmentStatus to : ShipmentStatus.values()) {
            assertThat(validator.isAllowed(ShipmentStatus.DELIVERED, to))
                    .as("DELIVERED -> %s should be rejected", to)
                    .isFalse();
        }
    }

    @Test
    void terminalStatusCancelledCannotTransitionToAnything() {
        for (ShipmentStatus to : ShipmentStatus.values()) {
            assertThat(validator.isAllowed(ShipmentStatus.CANCELLED, to))
                    .as("CANCELLED -> %s should be rejected", to)
                    .isFalse();
        }
    }
}