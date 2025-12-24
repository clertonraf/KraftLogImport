package com.kraftlog.pdfimport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedWorkoutExerciseData {
    private String exerciseName;
    private Integer sets;
    private Integer repetitions;
    private String advancedTechnique;
}
