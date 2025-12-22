package com.kraftlog.pdfimport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParsedExerciseData {
    private String name;
    private String videoUrl;
    private String muscleGroupPortuguese;
}
