package com.kraftlog.pdfimport.controller;

import com.kraftlog.pdfimport.service.ExerciseImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
@Tag(name = "PDF Import", description = "APIs for importing exercises from PDF files to KraftLog API")
public class ImportController {

    private final ExerciseImportService exerciseImportService;

    @Operation(summary = "Import exercises from PDF file",
               description = "Upload a PDF file containing exercise data in Portuguese format. " +
                           "The PDF should have muscle group headers and exercise tables with video URLs. " +
                           "Exercises will be sent to the configured KraftLog API.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Import completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid PDF format or no exercises found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/pdf")
    public ResponseEntity<Map<String, Object>> importExercisesFromPdf(
            @RequestParam("file") MultipartFile file) {
        
        log.info("Received request to import exercises from PDF: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "File is empty"
            ));
        }
        
        if (!Objects.requireNonNull(file.getOriginalFilename()).toLowerCase().endsWith(".pdf")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "File must be a PDF"
            ));
        }
        
        try {
            Path tempFile = Files.createTempFile("exercise-import-", ".pdf");
            file.transferTo(tempFile.toFile());
            
            try {
                ExerciseImportService.ImportResult result = 
                        exerciseImportService.importExercisesFromPdf(tempFile.toFile());
                
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Import completed",
                        "totalProcessed", result.getTotalCount(),
                        "successful", result.getSuccessCount(),
                        "failed", result.getFailureCount(),
                        "failures", result.getFailures()
                ));
            } finally {
                Files.deleteIfExists(tempFile);
            }
            
        } catch (IOException e) {
            log.error("Failed to process PDF file", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to process PDF: " + e.getMessage()
            ));
        } catch (IllegalArgumentException e) {
            log.error("Invalid input", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    @Operation(summary = "Health check", description = "Check if the service is running")
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "KraftLog PDF Import"
        ));
    }
}
