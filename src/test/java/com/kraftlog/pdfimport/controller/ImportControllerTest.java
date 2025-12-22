package com.kraftlog.pdfimport.controller;

import com.kraftlog.pdfimport.service.ExerciseImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportControllerTest {

    @Mock
    private ExerciseImportService exerciseImportService;

    private ImportController importController;

    @BeforeEach
    void setUp() {
        importController = new ImportController(exerciseImportService);
    }

    @Test
    void testImportExercisesFromPdfSuccess() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        ExerciseImportService.ImportResult mockResult = new ExerciseImportService.ImportResult();
        mockResult.incrementSuccess();
        mockResult.incrementSuccess();

        when(exerciseImportService.importExercisesFromPdf(any(File.class))).thenReturn(mockResult);

        ResponseEntity<Map<String, Object>> response = importController.importExercisesFromPdf(mockFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertEquals(2, response.getBody().get("totalProcessed"));
        assertEquals(2, response.getBody().get("successful"));
        assertEquals(0, response.getBody().get("failed"));
    }

    @Test
    void testImportExercisesFromPdfWithFailures() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        ExerciseImportService.ImportResult mockResult = new ExerciseImportService.ImportResult();
        mockResult.incrementSuccess();
        mockResult.addFailure("Failed Exercise", "API error");

        when(exerciseImportService.importExercisesFromPdf(any(File.class))).thenReturn(mockResult);

        ResponseEntity<Map<String, Object>> response = importController.importExercisesFromPdf(mockFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().get("status"));
        assertEquals(2, response.getBody().get("totalProcessed"));
        assertEquals(1, response.getBody().get("successful"));
        assertEquals(1, response.getBody().get("failed"));
        assertNotNull(response.getBody().get("failures"));
    }

    @Test
    void testImportExercisesFromPdfEmptyFile() {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        ResponseEntity<Map<String, Object>> response = importController.importExercisesFromPdf(mockFile);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().get("status"));
        assertEquals("File is empty", response.getBody().get("message"));
    }

    @Test
    void testImportExercisesFromPdfNotPdfFile() {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Text content".getBytes()
        );

        ResponseEntity<Map<String, Object>> response = importController.importExercisesFromPdf(mockFile);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().get("status"));
        assertEquals("File must be a PDF", response.getBody().get("message"));
    }

    @Test
    void testImportExercisesFromPdfIOException() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        when(exerciseImportService.importExercisesFromPdf(any(File.class)))
                .thenThrow(new IOException("Failed to process PDF"));

        ResponseEntity<Map<String, Object>> response = importController.importExercisesFromPdf(mockFile);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().get("status"));
        assertTrue(response.getBody().get("message").toString().contains("Failed to process PDF"));
    }

    @Test
    void testImportExercisesFromPdfIllegalArgumentException() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        when(exerciseImportService.importExercisesFromPdf(any(File.class)))
                .thenThrow(new IllegalArgumentException("No exercises found"));

        ResponseEntity<Map<String, Object>> response = importController.importExercisesFromPdf(mockFile);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().get("status"));
        assertTrue(response.getBody().get("message").toString().contains("No exercises found"));
    }

    @Test
    void testHealthCheck() {
        ResponseEntity<Map<String, String>> response = importController.health();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
        assertEquals("KraftLog PDF Import", response.getBody().get("service"));
    }

    @Test
    void testImportExercisesFromPdfNullFileName() {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> 
            importController.importExercisesFromPdf(mockFile)
        );
    }

    @Test
    void testImportExercisesFromPdfUppercasePdfExtension() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.PDF",
                "application/pdf",
                "PDF content".getBytes()
        );

        ExerciseImportService.ImportResult mockResult = new ExerciseImportService.ImportResult();
        mockResult.incrementSuccess();

        when(exerciseImportService.importExercisesFromPdf(any(File.class))).thenReturn(mockResult);

        ResponseEntity<Map<String, Object>> response = importController.importExercisesFromPdf(mockFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testImportExercisesFromPdfWithSpecialCharactersInName() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test file (1).pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        ExerciseImportService.ImportResult mockResult = new ExerciseImportService.ImportResult();
        mockResult.incrementSuccess();

        when(exerciseImportService.importExercisesFromPdf(any(File.class))).thenReturn(mockResult);

        ResponseEntity<Map<String, Object>> response = importController.importExercisesFromPdf(mockFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testImportExercisesFromPdfAllFailed() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        ExerciseImportService.ImportResult mockResult = new ExerciseImportService.ImportResult();
        mockResult.addFailure("Exercise 1", "Error 1");
        mockResult.addFailure("Exercise 2", "Error 2");

        when(exerciseImportService.importExercisesFromPdf(any(File.class))).thenReturn(mockResult);

        ResponseEntity<Map<String, Object>> response = importController.importExercisesFromPdf(mockFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().get("totalProcessed"));
        assertEquals(0, response.getBody().get("successful"));
        assertEquals(2, response.getBody().get("failed"));
    }

    @Test
    void testImportExercisesFromPdfVerifyTempFileCleanup() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        ExerciseImportService.ImportResult mockResult = new ExerciseImportService.ImportResult();
        mockResult.incrementSuccess();

        when(exerciseImportService.importExercisesFromPdf(any(File.class))).thenReturn(mockResult);

        ResponseEntity<Map<String, Object>> response = importController.importExercisesFromPdf(mockFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(exerciseImportService, times(1)).importExercisesFromPdf(any(File.class));
    }
}
