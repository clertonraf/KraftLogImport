package com.kraftlog.pdfimport.controller;

import com.kraftlog.pdfimport.dto.RoutineImportResult;
import com.kraftlog.pdfimport.service.RoutineImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/routine-import")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Routine Import", description = "Import workout routines from XLSX files")
public class RoutineImportController {

    private final RoutineImportService routineImportService;

    @PostMapping(value = "/generate-json", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Generate JSON from XLSX routine",
            description = "Parse XLSX file and generate a JSON structure with routine, workouts, and exercises"
    )
    @ApiResponse(responseCode = "200", description = "JSON generated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid file format or content")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public ResponseEntity<Map<String, Object>> generateJsonFromXlsx(
            @Parameter(description = "XLSX file containing the routine", required = true)
            @RequestParam("file") MultipartFile file) {
        
        log.info("Received request to generate JSON from XLSX: {}", file.getOriginalFilename());
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate file
            if (file.isEmpty()) {
                response.put("error", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".xlsx")) {
                response.put("error", "Only XLSX files are supported");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Generate JSON
            String json = routineImportService.generateRoutineJson(
                    file.getInputStream(), 
                    originalFilename);
            
            response.put("success", true);
            response.put("fileName", originalFilename);
            response.put("json", json);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Failed to generate JSON from XLSX file: {}", file.getOriginalFilename(), e);
            response.put("error", "Failed to process file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid file content: {}", e.getMessage());
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Import routine from XLSX to KraftLog API",
            description = "Parse XLSX file and import the complete routine with workouts and exercises to KraftLog API"
    )
    @ApiResponse(
            responseCode = "200", 
            description = "Import completed",
            content = @Content(schema = @Schema(implementation = RoutineImportResult.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid file format or content")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public ResponseEntity<Map<String, Object>> importRoutineFromXlsx(
            @Parameter(description = "XLSX file containing the routine", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "User ID for the routine", required = true)
            @RequestParam("userId") String userId) {
        
        log.info("Received request to import routine from XLSX: {} for user: {}", 
                file.getOriginalFilename(), userId);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate file
            if (file.isEmpty()) {
                response.put("error", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".xlsx")) {
                response.put("error", "Only XLSX files are supported");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate userId
            if (userId == null || userId.trim().isEmpty()) {
                response.put("error", "User ID is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Import routine
            RoutineImportResult result = routineImportService.importRoutineFromXlsx(
                    file.getInputStream(), 
                    originalFilename,
                    userId);
            
            response.put("success", result.getFailedWorkouts() == 0 && result.getFailedExercises() == 0);
            response.put("result", result);
            
            if (result.getFailedWorkouts() > 0 || result.getFailedExercises() > 0) {
                response.put("message", "Import completed with some failures");
            } else {
                response.put("message", "Import completed successfully");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Failed to import routine from XLSX file: {}", file.getOriginalFilename(), e);
            response.put("error", "Failed to process file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid file content: {}", e.getMessage());
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
