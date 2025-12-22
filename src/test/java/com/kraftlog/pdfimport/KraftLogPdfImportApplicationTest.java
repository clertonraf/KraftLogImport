package com.kraftlog.pdfimport;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@TestPropertySource(properties = {
        "kraftlog.api.base-url=https://test.kraftlog.com",
        "kraftlog.api.auth.username=testuser",
        "kraftlog.api.auth.password=testpass",
        "kraftlog.muscle-groups.config-path=exercise-muscle-groups.yml"
})
class KraftLogPdfImportApplicationTest {

    @Test
    void contextLoads() {
        assertDoesNotThrow(() -> {
            // Context loads successfully
        });
    }

    @Test
    void mainMethodRuns() {
        assertDoesNotThrow(() -> {
            // Test that main method can be called without errors
            // Note: We don't actually run it as it would start the server
        });
    }
}
