package com.example.shipment_tracking_system.service;

import com.example.shipment_tracking_system.dto.response.ImportReportResponse;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentImportService {

    private static final int EXPECTED_COLUMNS = 9;

    private final UserRepository userRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentStatusHistoryRepository statusHistoryRepository;
    private final TrackingNumberGenerator trackingNumberGenerator;

    @Transactional
    public ImportReportResponse importCsv(MultipartFile file) {
        List<String[]> rows = parseRows(file);
        int successCount = 0;

        for (int i = 0; i < rows.size(); i++) {
            int rowNumber = i + 2;
            String[] columns = rows.get(i);

            if (columns.length < EXPECTED_COLUMNS) {
                throw new CsvParsingException("Row " + rowNumber + ": expected " + EXPECTED_COLUMNS + " columns, found " + columns.length);
            }

            String email = columns[0].trim();
            String firstName = columns[1].trim();
            String lastName = columns[2].trim();
            String phone = columns[3].trim();
            String description = columns[4].trim();
            String origin = columns[5].trim();
            String destination = columns[6].trim();
            String weightRaw = columns[7].trim();
            String statusRaw = columns[8].trim();

            if (email.isBlank() || description.isBlank() || origin.isBlank() || destination.isBlank()) {
                throw new CsvParsingException("Row " + rowNumber + ": email, description, origin, and destination are required");
            }

            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                if (firstName.isBlank() || lastName.isBlank()) {
                    throw new CsvParsingException("Row " + rowNumber + ": new user requires firstName and lastName for email " + email);
                }
                user = userRepository.save(User.builder()
                        .email(email)
                        .firstName(firstName)
                        .lastName(lastName)
                        .phone(phone.isBlank() ? null : phone)
                        .build());
            }

            BigDecimal weightKg = null;
            if (!weightRaw.isBlank()) {
                try {
                    weightKg = new BigDecimal(weightRaw);
                } catch (NumberFormatException e) {
                    throw new CsvParsingException("Row " + rowNumber + ": invalid weight value " + weightRaw);
                }
            }

            ShipmentStatus status = ShipmentStatus.CREATED;
            if (!statusRaw.isBlank()) {
                try {
                    status = ShipmentStatus.valueOf(statusRaw.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new CsvParsingException("Row " + rowNumber + ": invalid status value " + statusRaw);
                }
            }

            Shipment shipment = shipmentRepository.save(Shipment.builder()
                    .user(user)
                    .trackingNumber(trackingNumberGenerator.generate())
                    .description(description)
                    .origin(origin)
                    .destination(destination)
                    .weightKg(weightKg)
                    .currentStatus(status)
                    .build());

            String note = status == ShipmentStatus.CREATED
                    ? "Created via CSV import"
                    : "Imported with status " + status + " via CSV — prior transitions not tracked";

            statusHistoryRepository.save(ShipmentStatusHistory.builder()
                    .shipment(shipment)
                    .status(status)
                    .note(note)
                    .build());

            successCount++;
        }

        log.info("CSV import succeeded: {} shipments created", successCount);
        return new ImportReportResponse(rows.size(), successCount);
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
            throw new CsvParsingException("Malformed CSV file: " + e.getMessage());
        }
    }
}