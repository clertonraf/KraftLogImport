package com.kraftlog.pdfimport.service;

import com.kraftlog.pdfimport.client.KraftLogApiClient;
import com.kraftlog.pdfimport.config.MuscleGroupMappingConfig;
import com.kraftlog.pdfimport.dto.ExerciseCreateRequest;
import com.kraftlog.pdfimport.dto.ParsedExerciseData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExerciseImportService {

    private final PdfParserService pdfParser;
    private final KraftLogApiClient apiClient;
    private final MuscleGroupMappingConfig muscleGroupConfig;

    public ImportResult importExercisesFromPdf(File pdfFile) throws IOException {
        log.info("Starting exercise import from PDF: {}", pdfFile.getName());
        
        List<ParsedExerciseData> parsedExercises = pdfParser.parseExercisesFromPdf(pdfFile);
        
        if (parsedExercises.isEmpty()) {
            throw new IllegalArgumentException("No exercises found in PDF file");
        }
        
        log.info("Parsed {} exercises from PDF, starting import to KraftLog API", parsedExercises.size());
        
        ImportResult result = new ImportResult();
        
        for (ParsedExerciseData parsedExercise : parsedExercises) {
            try {
                ExerciseCreateRequest request = convertToCreateRequest(parsedExercise);
                
                ParsedExerciseData created = apiClient.createExercise(request);
                
                if (created != null && created.getId() != null) {
                    result.incrementSuccess();
                } else {
                    result.addFailure(parsedExercise.getName(), "API returned error");
                }
                
            } catch (Exception e) {
                log.warn("Failed to import exercise: {} - {}", parsedExercise.getName(), e.getMessage());
                result.addFailure(parsedExercise.getName(), e.getMessage());
            }
        }
        
        log.info("Exercise import completed. Success: {}, Failed: {}", 
                result.getSuccessCount(), result.getFailureCount());
        
        return result;
    }

    private ExerciseCreateRequest convertToCreateRequest(ParsedExerciseData parsedExercise) {
        String muscleGroupEnglish = muscleGroupConfig.getMuscleGroupEnglishName(
                parsedExercise.getMuscleGroupPortuguese());
        
        if (muscleGroupEnglish != null) {
            log.debug("Mapped muscle group {} -> {}", 
                    parsedExercise.getMuscleGroupPortuguese(), muscleGroupEnglish);
        }
        
        return ExerciseCreateRequest.builder()
                .name(parsedExercise.getName())
                .videoUrl(parsedExercise.getVideoUrl())
                .muscleGroup(muscleGroupEnglish)
                .build();
    }

    public static class ImportResult {
        private int successCount = 0;
        private List<ImportFailure> failures = new ArrayList<>();
        
        public void incrementSuccess() {
            successCount++;
        }
        
        public void addFailure(String exerciseName, String reason) {
            failures.add(new ImportFailure(exerciseName, reason));
        }
        
        public int getSuccessCount() {
            return successCount;
        }
        
        public int getFailureCount() {
            return failures.size();
        }
        
        public int getTotalCount() {
            return successCount + failures.size();
        }
        
        public List<ImportFailure> getFailures() {
            return failures;
        }
    }

    public static class ImportFailure {
        private final String exerciseName;
        private final String reason;
        
        public ImportFailure(String exerciseName, String reason) {
            this.exerciseName = exerciseName;
            this.reason = reason;
        }
        
        public String getExerciseName() {
            return exerciseName;
        }
        
        public String getReason() {
            return reason;
        }
    }
}
