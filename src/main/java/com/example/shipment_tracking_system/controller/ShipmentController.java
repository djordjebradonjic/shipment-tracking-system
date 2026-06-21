package com.example.shipment_tracking_system.controller;

import com.example.shipment_tracking_system.dto.request.ShipmentStatusUpdateRequest;
import com.example.shipment_tracking_system.dto.response.ShipmentStatusHistoryResponse;

import com.example.shipment_tracking_system.dto.response.ImportReportResponse;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import com.example.shipment_tracking_system.dto.request.ShipmentCreateRequest;
import com.example.shipment_tracking_system.dto.response.ShipmentResponse;
import com.example.shipment_tracking_system.service.ShipmentImportService;
import com.example.shipment_tracking_system.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import com.example.shipment_tracking_system.model.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.Instant;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final ShipmentImportService shipmentImportService;

    @PostMapping
    public ResponseEntity<ShipmentResponse> create(@Valid @RequestBody ShipmentCreateRequest request) {
        ShipmentResponse response = shipmentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(shipmentService.getById(id));
    }


    @PatchMapping("/{id}/status")
    public ResponseEntity<ShipmentResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody ShipmentStatusUpdateRequest request) {
        return ResponseEntity.ok(shipmentService.updateStatus(id, request));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<ShipmentStatusHistoryResponse>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(shipmentService.getHistory(id));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportReportResponse> importCsv(@RequestParam("file") MultipartFile file) {
        ImportReportResponse report = shipmentImportService.importCsv(file);
        HttpStatus status = report.getErrors().isEmpty() ? HttpStatus.CREATED : HttpStatus.UNPROCESSABLE_ENTITY;
        return ResponseEntity.status(status).body(report);
    }

    @GetMapping
    public ResponseEntity<Page<ShipmentResponse>> search(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) ShipmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdTo,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(shipmentService.search(userId, status, createdFrom, createdTo, pageable));
    }
}