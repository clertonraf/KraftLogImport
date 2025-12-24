package com.kraftlog.pdfimport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutineImportResult {
    private String routineName;
    private String routineId;
    private Integer totalWorkouts;
    private Integer successfulWorkouts;
    private Integer failedWorkouts;
    private Integer totalExercises;
    private Integer successfulExercises;
    private Integer failedExercises;
    private List<String> errors;
}
