package com.example.shipment_tracking_system.service;

import com.example.shipment_tracking_system.dto.response.ImportReportResponse;
import com.example.shipment_tracking_system.exception.CsvParsingException;
import com.example.shipment_tracking_system.repository.ShipmentRepository;
import com.example.shipment_tracking_system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ShipmentImportServiceIntegrationTest {

    @Autowired
    private ShipmentImportService shipmentImportService;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void importCsv_validFile_createsShipmentsAndUsers() throws IOException {
        long shipmentsBefore = shipmentRepository.count();
        long usersBefore = userRepository.count();

        MockMultipartFile file = loadTestFile("test-import-valid.csv");
        ImportReportResponse report = shipmentImportService.importCsv(file);

        assertThat(report.getTotalRows()).isEqualTo(3);
        assertThat(report.getSuccessCount()).isEqualTo(3);
        assertThat(shipmentRepository.count()).isEqualTo(shipmentsBefore + 3);
        assertThat(userRepository.count()).isEqualTo(usersBefore + 2);
    }

    @Test
    void importCsv_invalidRow_throwsCsvParsingException() throws IOException {
        MockMultipartFile file = loadTestFile("test-import-invalid.csv");

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> shipmentImportService.importCsv(file))
                .isInstanceOf(CsvParsingException.class)
                .hasMessageContaining("firstName");
    }

    private MockMultipartFile loadTestFile(String filename) throws IOException {
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream(filename)) {
            assertThat(is)
                    .as("Test file '%s' not found in src/test/resources/", filename)
                    .isNotNull();
            return new MockMultipartFile(
                    "file", filename, "text/csv", is.readAllBytes());
        }
    }
}