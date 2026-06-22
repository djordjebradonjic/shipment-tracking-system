package com.example.shipment_tracking_system.service;

import com.example.shipment_tracking_system.dto.request.ShipmentStatusUpdateRequest;
import com.example.shipment_tracking_system.dto.response.ShipmentStatusHistoryResponse;
import com.example.shipment_tracking_system.exception.InvalidStatusTransitionException;

import com.example.shipment_tracking_system.model.*;
import com.example.shipment_tracking_system.repository.ShipmentSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.time.Instant;

import com.example.shipment_tracking_system.dto.request.ShipmentCreateRequest;
import com.example.shipment_tracking_system.dto.response.ShipmentResponse;
import com.example.shipment_tracking_system.exception.ResourceNotFoundException;
import com.example.shipment_tracking_system.mapper.ShipmentMapper;
import com.example.shipment_tracking_system.repository.ShipmentRepository;
import com.example.shipment_tracking_system.repository.ShipmentStatusHistoryRepository;
import com.example.shipment_tracking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentStatusHistoryRepository statusHistoryRepository;
    private final UserRepository userRepository;
    private final ShipmentMapper shipmentMapper;
    private final ShipmentStatusTransitionValidator transitionValidator;
    private final TrackingNumberGenerator trackingNumberGenerator;

    public ShipmentResponse create(ShipmentCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Shipment shipment = shipmentMapper.toEntity(request);
        shipment.setUser(user);
        shipment.setTrackingNumber(trackingNumberGenerator.generate());
        shipment.setCurrentStatus(ShipmentStatus.CREATED);

        Shipment saved = shipmentRepository.save(shipment);

        ShipmentStatusHistory history = ShipmentStatusHistory.builder()
                .shipment(saved)
                .status(ShipmentStatus.CREATED)
                .note("Shipment created")
                .build();
        statusHistoryRepository.save(history);

        log.info("Created shipment {} for user {}", saved.getTrackingNumber(), user.getId());

        return shipmentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getById(Long id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + id));
        return shipmentMapper.toResponse(shipment);
    }

    @Transactional(readOnly = true)
    public List<ShipmentResponse> getAll() {
        return shipmentRepository.findAll().stream()
                .map(shipmentMapper::toResponse)
                .toList();
    }

    public ShipmentResponse updateStatus(Long id, ShipmentStatusUpdateRequest request) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + id));

        ShipmentStatus currentStatus = shipment.getCurrentStatus();
        ShipmentStatus newStatus = request.getStatus();

        if (!transitionValidator.isAllowed(currentStatus, newStatus)) {
            throw new InvalidStatusTransitionException(
                    "Cannot transition shipment from " + currentStatus + " to " + newStatus);
        }

        shipment.setCurrentStatus(newStatus);
        Shipment saved = shipmentRepository.save(shipment);

        ShipmentStatusHistory history = ShipmentStatusHistory.builder()
                .shipment(saved)
                .status(newStatus)
                .note(request.getNote())
                .build();
        statusHistoryRepository.save(history);

        log.info("Shipment {} transitioned from {} to {}", saved.getTrackingNumber(), currentStatus, newStatus);

        return shipmentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ShipmentStatusHistoryResponse> getHistory(Long id) {
        if (!shipmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Shipment not found with id: " + id);
        }
        return statusHistoryRepository.findByShipmentIdOrderByChangedAtAsc(id).stream()
                .map(shipmentMapper::toHistoryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ShipmentResponse> search(Long userId, ShipmentStatus status, Instant createdFrom, Instant createdTo, Pageable pageable) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        if (currentUser.getRole() == Role.CUSTOMER) {
            userId = currentUser.getId();

        }
            Specification<Shipment> spec = Specification.where(ShipmentSpecifications.hasUserId(userId))
                .and(ShipmentSpecifications.hasStatus(status))
                .and(ShipmentSpecifications.createdAfter(createdFrom))
                .and(ShipmentSpecifications.createdBefore(createdTo))
                .and(ShipmentSpecifications.fetchUser());

        return shipmentRepository.findAll(spec, pageable)
                .map(shipmentMapper::toResponse);
    }




}