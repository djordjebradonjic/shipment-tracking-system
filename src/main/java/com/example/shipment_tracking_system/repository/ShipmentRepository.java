package com.example.shipment_tracking_system.repository;

import com.example.shipment_tracking_system.model.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

}
