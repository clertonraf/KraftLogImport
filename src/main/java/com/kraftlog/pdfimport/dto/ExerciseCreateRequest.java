package com.kraftlog.pdfimport.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseCreateRequest {

    @NotBlank(message = "Exercise name is required")
    private String name;

    private String description;

    private Integer sets;

    private Integer repetitions;

    private String technique;

    private Double defaultWeightKg;
    
    private String videoUrl;

    private String equipmentType;

    private String muscleGroup;

    private List<UUID> muscleIds;
}
