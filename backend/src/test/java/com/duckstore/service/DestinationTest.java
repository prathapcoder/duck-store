package com.duckstore.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.duckstore.common.Destination;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DestinationTest {

    @ParameterizedTest
    @CsvSource({"USA,USA", "usa,USA", "' U.S.A. ',USA", "United States,USA", "us,USA",
            "Bolivia,BOLIVIA", "BO,BOLIVIA", "India,INDIA", "IND,INDIA",
            "France,OTHER", "Japan,OTHER", "'',OTHER"})
    void recognises_names_codes_and_aliases(String input, Destination expected) {
        assertThat(Destination.from(input)).isEqualTo(expected);
    }
}
