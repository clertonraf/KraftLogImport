package com.kraftlog.pdfimport.service;

import com.kraftlog.pdfimport.client.KraftLogApiClient;
import com.kraftlog.pdfimport.config.MuscleGroupMappingConfig;
import com.kraftlog.pdfimport.dto.ExerciseCreateRequest;
import com.kraftlog.pdfimport.dto.ParsedExerciseData;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
    "kraftlog.muscle-groups.config-path=exercise-muscle-groups.yml"
})
class ExerciseImportServiceTest {

    @MockBean
    private PdfParserService pdfParser;

    @MockBean
    private KraftLogApiClient apiClient;

    @Autowired
    private MuscleGroupMappingConfig muscleGroupConfig;

    @Autowired
    private ExerciseImportService exerciseImportService;

    @Test
    void testImportExercisesFromPdfSuccess() throws Exception {
        File mockFile = mock(File.class);
        when(mockFile.getName()).thenReturn("test.pdf");

        List<ParsedExerciseData> parsedExercises = List.of(
                ParsedExerciseData.builder()
                        .name("Bench Press")
                        .videoUrl("https://youtube.com/watch?v=test1")
                        .muscleGroupPortuguese("PEITO")
                        .build(),
                ParsedExerciseData.builder()
                        .name("Squat")
                        .videoUrl("https://youtube.com/watch?v=test2")
                        .muscleGroupPortuguese("PERNAS")
                        .build()
        );

        when(pdfParser.parseExercisesFromPdf(mockFile)).thenReturn(parsedExercises);
        ParsedExerciseData created = ParsedExerciseData.builder()
                .id("ex1")
                .name("Test")
                .build();
        when(apiClient.createExercise(any(ExerciseCreateRequest.class))).thenReturn(created);

        ExerciseImportService.ImportResult result = exerciseImportService.importExercisesFromPdf(mockFile);

        assertEquals(2, result.getTotalCount());
        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        verify(apiClient, times(2)).createExercise(any(ExerciseCreateRequest.class));
    }

    @Test
    void testImportExercisesFromPdfWithFailures() throws Exception {
        File mockFile = mock(File.class);
        when(mockFile.getName()).thenReturn("test.pdf");

        List<ParsedExerciseData> parsedExercises = List.of(
                ParsedExerciseData.builder()
                        .name("Success Exercise")
                        .videoUrl("https://youtube.com/watch?v=test1")
                        .muscleGroupPortuguese("PEITO")
                        .build(),
                ParsedExerciseData.builder()
                        .name("Failed Exercise")
                        .videoUrl("https://youtube.com/watch?v=test2")
                        .muscleGroupPortuguese("PERNAS")
                        .build()
        );

        when(pdfParser.parseExercisesFromPdf(mockFile)).thenReturn(parsedExercises);
        ParsedExerciseData created = ParsedExerciseData.builder()
                .id("ex1")
                .name("Test")
                .build();
        when(apiClient.createExercise(any(ExerciseCreateRequest.class)))
                .thenReturn(created)
                .thenReturn(null);

        ExerciseImportService.ImportResult result = exerciseImportService.importExercisesFromPdf(mockFile);

        assertEquals(2, result.getTotalCount());
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertEquals("Failed Exercise", result.getFailures().get(0).getExerciseName());
    }

    @Test
    void testImportExercisesFromPdfWithException() throws Exception {
        File mockFile = mock(File.class);
        when(mockFile.getName()).thenReturn("test.pdf");

        List<ParsedExerciseData> parsedExercises = List.of(
                ParsedExerciseData.builder()
                        .name("Exception Exercise")
                        .videoUrl("https://youtube.com/watch?v=test")
                        .muscleGroupPortuguese("PEITO")
                        .build()
        );

        when(pdfParser.parseExercisesFromPdf(mockFile)).thenReturn(parsedExercises);
        when(apiClient.createExercise(any(ExerciseCreateRequest.class)))
                .thenThrow(new RuntimeException("API error"));

        ExerciseImportService.ImportResult result = exerciseImportService.importExercisesFromPdf(mockFile);

        assertEquals(1, result.getTotalCount());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertTrue(result.getFailures().get(0).getReason().contains("API error"));
    }

    @Test
    void testImportExercisesFromPdfNoExercisesFound() throws Exception {
        File mockFile = mock(File.class);
        when(mockFile.getName()).thenReturn("empty.pdf");

        when(pdfParser.parseExercisesFromPdf(mockFile)).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () -> 
            exerciseImportService.importExercisesFromPdf(mockFile)
        );
    }

    @Test
    void testConvertToCreateRequestWithMuscleGroupMapping() throws Exception {
        File mockFile = mock(File.class);
        when(mockFile.getName()).thenReturn("test.pdf");

        ParsedExerciseData parsedData = ParsedExerciseData.builder()
                .name("Bench Press")
                .videoUrl("https://youtube.com/watch?v=test")
                .muscleGroupPortuguese("PEITO")
                .build();

        when(pdfParser.parseExercisesFromPdf(mockFile)).thenReturn(List.of(parsedData));
        // muscleGroupConfig is autowired and will use the real configuration
        ParsedExerciseData created = ParsedExerciseData.builder()
                .id("ex1")
                .name("Bench Press")
                .build();
        when(apiClient.createExercise(any(ExerciseCreateRequest.class))).thenReturn(created);

        exerciseImportService.importExercisesFromPdf(mockFile);

        ArgumentCaptor<ExerciseCreateRequest> captor = ArgumentCaptor.forClass(ExerciseCreateRequest.class);
        verify(apiClient).createExercise(captor.capture());

        ExerciseCreateRequest request = captor.getValue();
        assertEquals("Bench Press", request.getName());
        assertEquals("https://youtube.com/watch?v=test", request.getVideoUrl());
    }

    @Test
    void testConvertToCreateRequestWithoutMuscleGroupMapping() throws Exception {
        File mockFile = mock(File.class);
        when(mockFile.getName()).thenReturn("test.pdf");

        ParsedExerciseData parsedData = ParsedExerciseData.builder()
                .name("Squat")
                .videoUrl("https://youtube.com/watch?v=test")
                .muscleGroupPortuguese("UNKNOWN_MUSCLE")
                .build();

        when(pdfParser.parseExercisesFromPdf(mockFile)).thenReturn(List.of(parsedData));
        // muscleGroupConfig is autowired and will return null for unknown muscle groups
        ParsedExerciseData created = ParsedExerciseData.builder()
                .id("ex1")
                .name("Squat")
                .build();
        when(apiClient.createExercise(any(ExerciseCreateRequest.class))).thenReturn(created);

        exerciseImportService.importExercisesFromPdf(mockFile);

        ArgumentCaptor<ExerciseCreateRequest> captor = ArgumentCaptor.forClass(ExerciseCreateRequest.class);
        verify(apiClient).createExercise(captor.capture());

        ExerciseCreateRequest request = captor.getValue();
        assertEquals("Squat", request.getName());
    }

    @Test
    void testImportResultInitialState() {
        ExerciseImportService.ImportResult result = new ExerciseImportService.ImportResult();

        assertEquals(0, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        assertEquals(0, result.getTotalCount());
        assertTrue(result.getFailures().isEmpty());
    }

    @Test
    void testImportResultIncrementSuccess() {
        ExerciseImportService.ImportResult result = new ExerciseImportService.ImportResult();

        result.incrementSuccess();
        result.incrementSuccess();

        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        assertEquals(2, result.getTotalCount());
    }

    @Test
    void testImportResultAddFailure() {
        ExerciseImportService.ImportResult result = new ExerciseImportService.ImportResult();

        result.addFailure("Exercise 1", "Reason 1");
        result.addFailure("Exercise 2", "Reason 2");

        assertEquals(0, result.getSuccessCount());
        assertEquals(2, result.getFailureCount());
        assertEquals(2, result.getTotalCount());
        assertEquals(2, result.getFailures().size());
    }

    @Test
    void testImportResultMixedResults() {
        ExerciseImportService.ImportResult result = new ExerciseImportService.ImportResult();

        result.incrementSuccess();
        result.incrementSuccess();
        result.addFailure("Failed Exercise", "API error");

        assertEquals(2, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertEquals(3, result.getTotalCount());
    }

    @Test
    void testImportFailureGetters() {
        ExerciseImportService.ImportFailure failure = 
                new ExerciseImportService.ImportFailure("Test Exercise", "Test Reason");

        assertEquals("Test Exercise", failure.getExerciseName());
        assertEquals("Test Reason", failure.getReason());
    }

    @Test
    void testImportExercisesFromPdfIOException() throws Exception {
        File mockFile = mock(File.class);
        when(mockFile.getName()).thenReturn("test.pdf");

        when(pdfParser.parseExercisesFromPdf(mockFile)).thenThrow(new IOException("Cannot read file"));

        assertThrows(IOException.class, () -> 
            exerciseImportService.importExercisesFromPdf(mockFile)
        );
    }
}
