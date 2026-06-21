package com.example.shipment_tracking_system.service;

import com.example.shipment_tracking_system.dto.response.ImportReportResponse;
import com.example.shipment_tracking_system.dto.response.ImportRowError;
import com.example.shipment_tracking_system.exception.CsvParsingException;
import com.example.shipment_tracking_system.model.Shipment;
import com.example.shipment_tracking_system.model.ShipmentStatus;
import com.example.shipment_tracking_system.model.ShipmentStatusHistory;
import com.example.shipment_tracking_system.model.User;
import com.example.shipment_tracking_system.repository.ShipmentRepository;
import com.example.shipment_tracking_system.repository.ShipmentStatusHistoryRepository;
import com.example.shipment_tracking_system.repository.UserRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentImportService {

    private static final int EXPECTED_COLUMNS = 5;

    private final UserRepository userRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentStatusHistoryRepository statusHistoryRepository;
    private final TrackingNumberGenerator trackingNumberGenerator;

    @Transactional
    public ImportReportResponse importCsv(MultipartFile file) {
        List<String[]> rows = parseRows(file);
        List<ImportRowError> errors = new ArrayList<>();
        List<Shipment> validShipments = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            int rowNumber = i + 2;
            String[] columns = rows.get(i);

            if (columns.length < EXPECTED_COLUMNS) {
                errors.add(new ImportRowError(rowNumber, "Expected " + EXPECTED_COLUMNS + " columns, found " + columns.length));
                continue;
            }

            String email = columns[0].trim();
            String description = columns[1].trim();
            String origin = columns[2].trim();
            String destination = columns[3].trim();
            String weightRaw = columns[4].trim();

            if (email.isBlank() || description.isBlank() || origin.isBlank() || destination.isBlank()) {
                errors.add(new ImportRowError(rowNumber, "email, description, origin, and destination are required"));
                continue;
            }

            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                errors.add(new ImportRowError(rowNumber, "No user found with email: " + email));
                continue;
            }

            BigDecimal weightKg = null;
            if (!weightRaw.isBlank()) {
                try {
                    weightKg = new BigDecimal(weightRaw);
                } catch (NumberFormatException e) {
                    errors.add(new ImportRowError(rowNumber, "Invalid weight value: " + weightRaw));
                    continue;
                }
            }

            Shipment shipment = Shipment.builder()
                    .user(userOpt.get())
                    .trackingNumber(trackingNumberGenerator.generate())
                    .description(description)
                    .origin(origin)
                    .destination(destination)
                    .weightKg(weightKg)
                    .currentStatus(ShipmentStatus.CREATED)
                    .build();

            validShipments.add(shipment);
        }

        if (!errors.isEmpty()) {
            log.warn("CSV import rejected: {} of {} rows invalid", errors.size(), rows.size());
            return new ImportReportResponse(rows.size(), 0, errors);
        }

        List<Shipment> saved = shipmentRepository.saveAll(validShipments);

        List<ShipmentStatusHistory> histories = saved.stream()
                .map(s -> ShipmentStatusHistory.builder()
                        .shipment(s)
                        .status(ShipmentStatus.CREATED)
                        .note("Created via CSV import")
                        .build())
                .toList();
        statusHistoryRepository.saveAll(histories);

        log.info("CSV import succeeded: {} shipments created", saved.size());
        return new ImportReportResponse(rows.size(), saved.size(), List.of());
    }

    private List<String[]> parseRows(MultipartFile file) {
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<String[]> all = reader.readAll();
            if (all.isEmpty()) {
                return List.of();
            }
            return all.subList(1, all.size());
        } catch (IOException e) {
            throw new CsvParsingException("Could not read CSV file: " + e.getMessage());
        } catch (CsvException e) {
            throw new CsvParsingException("Invalidmo CSV file: " + e.getMessage());
        }
    }
}