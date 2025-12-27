package com.kraftlog.pdfimport.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParsedExerciseData {
    private String id;
    private String name;
    private String description;
    private Integer sets;
    private Integer repetitions;
    private String technique;
    private Double defaultWeightKg;
    private String videoUrl;
    private String equipmentType;
    private String muscleGroup;
    private String muscleGroupPortuguese;
    private List<MuscleData> muscles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MuscleData {
        private String id;
        private String name;
        private String scientificName;
        private String muscleGroup;
    }
}
