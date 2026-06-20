package com.example.shipment_tracking_system.repository;

import com.example.shipment_tracking_system.model.ShipmentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentStatusHistoryRepository extends JpaRepository<ShipmentStatusHistory, Long> {

    List<ShipmentStatusHistory> findByShipmentIdOrderByChangedAtAsc(Long shipmentId);

}
