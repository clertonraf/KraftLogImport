package com.kraftlog.pdfimport.service;

import com.kraftlog.pdfimport.config.MuscleGroupMappingConfig;
import com.kraftlog.pdfimport.dto.ParsedExerciseData;
import com.kraftlog.pdfimport.test.TestConfigHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "kraftlog.muscle-groups.config-path=exercise-muscle-groups.yml"
})
class PdfParserServiceTest {

    @Autowired
    private PdfParserService pdfParserService;

    @Autowired
    private MuscleGroupMappingConfig muscleGroupConfig;

    @Test
    void testParseExercisesFromPdfWithNonExistentFile() {
        File nonExistentFile = new File("/non/existent/file.pdf");
        
        assertThrows(IOException.class, () -> 
            pdfParserService.parseExercisesFromPdf(nonExistentFile)
        );
    }

    @Test
    void testParseExercisesFromPdfWithNullFile() {
        assertThrows(NullPointerException.class, () -> 
            pdfParserService.parseExercisesFromPdf(null)
        );
    }

    @Test
    void testConstructorWithNullConfig() {
        // PdfParserService uses Lombok's @RequiredArgsConstructor which doesn't add null checks
        // This test verifies that the service can be instantiated (though it will fail at runtime if config is used)
        assertDoesNotThrow(() -> new PdfParserService(null));
    }

    @Test
    void testMuscleGroupConfigIsInjected() {
        // Verify that Spring injected the muscle group config
        assertNotNull(muscleGroupConfig);
        assertNotNull(muscleGroupConfig.getMuscleGroupHeaders());
        assertFalse(muscleGroupConfig.getMuscleGroupHeaders().isEmpty());
    }

    @Test
    void testServiceInitialization() {
        // Verify that Spring created and injected the service
        assertNotNull(pdfParserService);
    }
}
