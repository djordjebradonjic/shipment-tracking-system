package com.example.shipment_tracking_system.repository;

import com.example.shipment_tracking_system.model.ShipmentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentStatusHistoryRepository extends JpaRepository<ShipmentStatusHistory, Long> {
}
