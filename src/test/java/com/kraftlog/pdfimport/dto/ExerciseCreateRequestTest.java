package com.kraftlog.pdfimport.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ExerciseCreateRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidExerciseCreateRequest() {
        ExerciseCreateRequest request = ExerciseCreateRequest.builder()
                .name("Bench Press")
                .description("Classic chest exercise")
                .sets(3)
                .repetitions(10)
                .technique("Keep back flat")
                .defaultWeightKg(60.0)
                .videoUrl("https://youtube.com/watch?v=test")
                .equipmentType("Barbell")
                .muscleIds(List.of(UUID.randomUUID()))
                .build();

        Set<ConstraintViolation<ExerciseCreateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testNameIsRequired() {
        ExerciseCreateRequest request = ExerciseCreateRequest.builder()
                .name(null)
                .build();

        Set<ConstraintViolation<ExerciseCreateRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("required"));
    }

    @Test
    void testBlankNameNotAllowed() {
        ExerciseCreateRequest request = ExerciseCreateRequest.builder()
                .name("   ")
                .build();

        Set<ConstraintViolation<ExerciseCreateRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void testBuilderPattern() {
        UUID muscleId = UUID.randomUUID();
        ExerciseCreateRequest request = ExerciseCreateRequest.builder()
                .name("Squat")
                .description("Leg exercise")
                .sets(4)
                .repetitions(12)
                .technique("Keep knees aligned")
                .defaultWeightKg(100.0)
                .videoUrl("https://youtube.com/watch?v=squat")
                .equipmentType("Barbell")
                .muscleIds(List.of(muscleId))
                .build();

        assertEquals("Squat", request.getName());
        assertEquals("Leg exercise", request.getDescription());
        assertEquals(4, request.getSets());
        assertEquals(12, request.getRepetitions());
        assertEquals("Keep knees aligned", request.getTechnique());
        assertEquals(100.0, request.getDefaultWeightKg());
        assertEquals("https://youtube.com/watch?v=squat", request.getVideoUrl());
        assertEquals("Barbell", request.getEquipmentType());
        assertEquals(1, request.getMuscleIds().size());
        assertEquals(muscleId, request.getMuscleIds().get(0));
    }

    @Test
    void testNoArgsConstructor() {
        ExerciseCreateRequest request = new ExerciseCreateRequest();
        assertNull(request.getName());
        assertNull(request.getDescription());
        assertNull(request.getSets());
    }

    @Test
    void testAllArgsConstructor() {
        UUID muscleId = UUID.randomUUID();
        ExerciseCreateRequest request = new ExerciseCreateRequest(
                "Deadlift",
                "Back exercise",
                5,
                5,
                "Keep back straight",
                120.0,
                "https://youtube.com/watch?v=deadlift",
                "Barbell",
                "Back",
                List.of(muscleId)
        );

        assertEquals("Deadlift", request.getName());
        assertEquals("Back exercise", request.getDescription());
        assertEquals(5, request.getSets());
        assertEquals(5, request.getRepetitions());
    }

    @Test
    void testSettersAndGetters() {
        ExerciseCreateRequest request = new ExerciseCreateRequest();
        UUID muscleId = UUID.randomUUID();
        
        request.setName("Pull-up");
        request.setDescription("Upper body exercise");
        request.setSets(3);
        request.setRepetitions(8);
        request.setTechnique("Full range of motion");
        request.setDefaultWeightKg(0.0);
        request.setVideoUrl("https://youtube.com/watch?v=pullup");
        request.setEquipmentType("Pull-up Bar");
        request.setMuscleIds(List.of(muscleId));

        assertEquals("Pull-up", request.getName());
        assertEquals("Upper body exercise", request.getDescription());
        assertEquals(3, request.getSets());
        assertEquals(8, request.getRepetitions());
        assertEquals("Full range of motion", request.getTechnique());
        assertEquals(0.0, request.getDefaultWeightKg());
        assertEquals("https://youtube.com/watch?v=pullup", request.getVideoUrl());
        assertEquals("Pull-up Bar", request.getEquipmentType());
        assertEquals(1, request.getMuscleIds().size());
    }

    @Test
    void testOptionalFieldsCanBeNull() {
        ExerciseCreateRequest request = ExerciseCreateRequest.builder()
                .name("Push-up")
                .build();

        Set<ConstraintViolation<ExerciseCreateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
        assertNull(request.getDescription());
        assertNull(request.getSets());
        assertNull(request.getRepetitions());
        assertNull(request.getTechnique());
        assertNull(request.getDefaultWeightKg());
        assertNull(request.getVideoUrl());
        assertNull(request.getEquipmentType());
        assertNull(request.getMuscleIds());
    }
}
