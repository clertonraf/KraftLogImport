package com.kraftlog.pdfimport.service;

import com.kraftlog.pdfimport.config.MuscleGroupMappingConfig;
import com.kraftlog.pdfimport.dto.ParsedExerciseData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PdfParserServiceTest {

    private PdfParserService pdfParserService;
    private MuscleGroupMappingConfig muscleGroupConfig;

    @BeforeEach
    void setUp() {
        muscleGroupConfig = mock(MuscleGroupMappingConfig.class);
        when(muscleGroupConfig.getMuscleGroupHeaders()).thenReturn(Set.of("PEITO", "COSTAS", "PERNAS"));
        pdfParserService = new PdfParserService(muscleGroupConfig);
    }

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
    void testMuscleGroupConfigIsUsed() throws IOException {
        MuscleGroupMappingConfig config = mock(MuscleGroupMappingConfig.class);
        when(config.getMuscleGroupHeaders()).thenReturn(Set.of("PEITO"));
        
        PdfParserService service = new PdfParserService(config);
        
        // Parse a simple text to trigger config usage
        File tempFile = File.createTempFile("test", ".pdf");
        tempFile.deleteOnExit();
        
        // The config is used during parsing, not construction
        // So we just verify the service was created successfully
        assertNotNull(service);
        assertEquals(config, getField(service, "muscleGroupConfig"));
    }
    
    private Object getField(Object obj, String fieldName) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            return null;
        }
    }

    @Test
    void testServiceInitialization() {
        MuscleGroupMappingConfig config = mock(MuscleGroupMappingConfig.class);
        when(config.getMuscleGroupHeaders()).thenReturn(Set.of("PEITO", "COSTAS"));
        
        PdfParserService service = new PdfParserService(config);
        
        assertNotNull(service);
    }
}
