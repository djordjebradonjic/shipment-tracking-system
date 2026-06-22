package com.example.shipment_tracking_system.service;

import org.junit.jupiter.api.Test;

import java.time.Year;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TrackingNumberGeneratorTest {

    private final TrackingNumberGenerator generator = new TrackingNumberGenerator();

    @Test
    void generatedNumberStartsWithHash() {
        String number = generator.generate();

        assertThat(number).startsWith("#");
    }

    @Test
    void generatedNumberContainsCurrentYear() {
        String number = generator.generate();
        String currentYear = String.valueOf(Year.now().getValue());

        assertThat(number).contains(currentYear);
    }

    @Test
    void generatedNumberMatchesExpectedFormat() {
        String number = generator.generate();
        String expectedPrefix = "#" + Year.now().getValue() + "-";

        assertThat(number).startsWith(expectedPrefix);
    }

    @Test
    void generatedNumberSuffixIsUpperCase() {
        String number = generator.generate();
        String suffix = number.substring(number.lastIndexOf("-") + 1);

        assertThat(suffix).isEqualTo(suffix.toUpperCase());
    }

    @Test
    void generatedNumbersAreUnique() {
        Set<String> numbers = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            numbers.add(generator.generate());
        }

        assertThat(numbers).hasSize(1000);
    }


}