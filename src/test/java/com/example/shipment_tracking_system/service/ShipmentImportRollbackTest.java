package com.example.shipment_tracking_system.service;

import com.example.shipment_tracking_system.exception.CsvParsingException;
import com.example.shipment_tracking_system.repository.ShipmentRepository;
import com.example.shipment_tracking_system.repository.ShipmentStatusHistoryRepository;
import com.example.shipment_tracking_system.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ShipmentImportRollbackTest {

    @Autowired
    private ShipmentImportService shipmentImportService;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShipmentStatusHistoryRepository statusHistoryRepository;

    @BeforeEach
    @AfterEach
    void cleanTestData() {
        statusHistoryRepository.deleteAll();
        shipmentRepository.deleteAll();
        userRepository.findByEmail("rollback.user1@test.com")
                .ifPresent(userRepository::delete);
        userRepository.findByEmail("rollback.newuser@test.com")
                .ifPresent(userRepository::delete);
    }

    @Test
    void importCsv_invalidRow_rollsBackEntireTransaction() throws IOException {
        long shipmentsBefore = shipmentRepository.count();
        long usersBefore = userRepository.count();

        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("test-import-rollback.csv")) {
            assertThat(is).isNotNull();
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test-import-rollback.csv",
                    "text/csv", is.readAllBytes());

            assertThatThrownBy(() -> shipmentImportService.importCsv(file))
                    .isInstanceOf(CsvParsingException.class)
                    .hasMessageContaining("firstName");
        }

        assertThat(shipmentRepository.count())
                .as("No shipments should remain after rollback")
                .isEqualTo(shipmentsBefore);
        assertThat(userRepository.count())
                .as("No users should remain after rollback")
                .isEqualTo(usersBefore);
    }
}