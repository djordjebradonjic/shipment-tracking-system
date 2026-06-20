package com.example.shipment_tracking_system.repository;

import com.example.shipment_tracking_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {

}
