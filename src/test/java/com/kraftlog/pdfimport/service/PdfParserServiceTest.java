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
        assertThrows(NullPointerException.class, () -> 
            new PdfParserService(null)
        );
    }

    @Test
    void testMuscleGroupConfigIsUsed() {
        MuscleGroupMappingConfig config = mock(MuscleGroupMappingConfig.class);
        when(config.getMuscleGroupHeaders()).thenReturn(Set.of("PEITO"));
        
        PdfParserService service = new PdfParserService(config);
        
        verify(config, atLeastOnce()).getMuscleGroupHeaders();
    }

    @Test
    void testServiceInitialization() {
        MuscleGroupMappingConfig config = mock(MuscleGroupMappingConfig.class);
        when(config.getMuscleGroupHeaders()).thenReturn(Set.of("PEITO", "COSTAS"));
        
        PdfParserService service = new PdfParserService(config);
        
        assertNotNull(service);
    }
}
