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
public class ParsedRoutineData {
    private String routineName;
    private List<ParsedWorkoutData> workouts;
}
