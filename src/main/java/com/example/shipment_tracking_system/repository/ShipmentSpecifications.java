package com.example.shipment_tracking_system.repository;

import com.example.shipment_tracking_system.model.Shipment;
import com.example.shipment_tracking_system.model.ShipmentStatus;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class ShipmentSpecifications {

    private ShipmentSpecifications() {
    }

    public static Specification<Shipment> hasUserId(Long userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Shipment> hasStatus(ShipmentStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("currentStatus"), status);
    }

    public static Specification<Shipment> createdAfter(Instant from) {
        return (root, query, cb) -> from == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<Shipment> createdBefore(Instant to) {
        return (root, query, cb) -> to == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }

    public static Specification<Shipment> fetchUser() {
        return (root, query, cb) -> {
            if (Long.class != query.getResultType()) {
                root.fetch("user", JoinType.LEFT);
            }
            return null;
        };
    }
}