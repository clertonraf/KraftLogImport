package com.kraftlog.pdfimport.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParsedExerciseDataTest {

    @Test
    void testBuilderPattern() {
        ParsedExerciseData data = ParsedExerciseData.builder()
                .name("Bench Press")
                .videoUrl("https://youtube.com/watch?v=test")
                .muscleGroupPortuguese("PEITO")
                .build();

        assertEquals("Bench Press", data.getName());
        assertEquals("https://youtube.com/watch?v=test", data.getVideoUrl());
        assertEquals("PEITO", data.getMuscleGroupPortuguese());
    }

    @Test
    void testNoArgsConstructor() {
        ParsedExerciseData data = new ParsedExerciseData();
        assertNull(data.getName());
        assertNull(data.getVideoUrl());
        assertNull(data.getMuscleGroupPortuguese());
    }

    @Test
    void testAllArgsConstructor() {
        ParsedExerciseData data = ParsedExerciseData.builder()
                .id("ex1")
                .name("Squat")
                .videoUrl("https://youtube.com/watch?v=squat")
                .muscleGroup("Legs")
                .muscleGroupPortuguese("PERNAS")
                .build();

        assertEquals("Squat", data.getName());
        assertEquals("https://youtube.com/watch?v=squat", data.getVideoUrl());
        assertEquals("PERNAS", data.getMuscleGroupPortuguese());
    }

    @Test
    void testSettersAndGetters() {
        ParsedExerciseData data = new ParsedExerciseData();
        
        data.setName("Deadlift");
        data.setVideoUrl("https://youtube.com/watch?v=deadlift");
        data.setMuscleGroupPortuguese("COSTAS");

        assertEquals("Deadlift", data.getName());
        assertEquals("https://youtube.com/watch?v=deadlift", data.getVideoUrl());
        assertEquals("COSTAS", data.getMuscleGroupPortuguese());
    }

    @Test
    void testWithNullValues() {
        ParsedExerciseData data = ParsedExerciseData.builder()
                .name("Exercise Without URL")
                .videoUrl(null)
                .muscleGroupPortuguese(null)
                .build();

        assertEquals("Exercise Without URL", data.getName());
        assertNull(data.getVideoUrl());
        assertNull(data.getMuscleGroupPortuguese());
    }

    @Test
    void testEqualsAndHashCode() {
        ParsedExerciseData data1 = ParsedExerciseData.builder()
                .name("Test")
                .videoUrl("https://test.com")
                .muscleGroupPortuguese("PEITO")
                .build();

        ParsedExerciseData data2 = ParsedExerciseData.builder()
                .name("Test")
                .videoUrl("https://test.com")
                .muscleGroupPortuguese("PEITO")
                .build();

        assertEquals(data1, data2);
        assertEquals(data1.hashCode(), data2.hashCode());
    }

    @Test
    void testToString() {
        ParsedExerciseData data = ParsedExerciseData.builder()
                .name("Test Exercise")
                .videoUrl("https://test.com")
                .muscleGroupPortuguese("PEITO")
                .build();

        String toString = data.toString();
        assertTrue(toString.contains("Test Exercise"));
        assertTrue(toString.contains("https://test.com"));
        assertTrue(toString.contains("PEITO"));
    }
}
