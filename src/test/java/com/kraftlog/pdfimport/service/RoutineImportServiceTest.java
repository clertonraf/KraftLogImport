package com.kraftlog.pdfimport.service;

import com.kraftlog.pdfimport.client.KraftLogApiClient;
import com.kraftlog.pdfimport.config.MuscleGroupMappingConfig;
import com.kraftlog.pdfimport.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
    "kraftlog.muscle-groups.config-path=exercise-muscle-groups.yml"
})
class RoutineImportServiceTest {

    @MockBean
    private XlsxParserService xlsxParserService;

    @MockBean
    private KraftLogApiClient kraftLogApiClient;

    @Autowired
    private MuscleGroupMappingConfig muscleGroupMappingConfig;

    @Autowired
    private RoutineImportService routineImportService;

    @Test
    void testGenerateRoutineJson_Success() throws Exception {
        // Setup
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        String fileName = "test-routine.xlsx";
        
        ParsedRoutineData parsedRoutine = createTestRoutine();
        when(xlsxParserService.parseRoutineFromXlsx(inputStream, fileName))
                .thenReturn(parsedRoutine);
        
        ParsedExerciseData apiExercise = ParsedExerciseData.builder()
                .id("ex1")
                .name("Supino Reto")
                .muscleGroup("Chest")
                .build();
        when(kraftLogApiClient.searchExercises(anyString()))
                .thenReturn(Collections.singletonList(apiExercise));
        
        // Execute
        String json = routineImportService.generateRoutineJson(inputStream, fileName);
        
        // Verify
        assertNotNull(json);
        assertTrue(json.contains("test"), "JSON should contain routine name 'test'");
        assertTrue(json.contains("Workout A"), "JSON should contain workout name");
        assertTrue(json.contains("Supino Reto"), "JSON should contain exercise name");
        assertTrue(json.contains("Chest"), "JSON should contain muscle group");
        
        verify(xlsxParserService).parseRoutineFromXlsx(inputStream, fileName);
        verify(kraftLogApiClient).searchExercises("Supino Reto");
    }

    @Test
    void testGenerateRoutineJson_ExerciseNotFoundInApi() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        String fileName = "test.xlsx";
        
        ParsedRoutineData parsedRoutine = createTestRoutine();
        when(xlsxParserService.parseRoutineFromXlsx(inputStream, fileName))
                .thenReturn(parsedRoutine);
        
        when(kraftLogApiClient.searchExercises(anyString()))
                .thenReturn(Collections.emptyList());
        
        String json = routineImportService.generateRoutineJson(inputStream, fileName);
        
        assertNotNull(json);
        assertTrue(json.contains("Supino Reto"));
    }

    @Test
    void testImportRoutineFromXlsx_Success() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        String fileName = "test.xlsx";
        String userId = "user123";
        
        ParsedRoutineData parsedRoutine = createTestRoutine();
        when(xlsxParserService.parseRoutineFromXlsx(inputStream, fileName))
                .thenReturn(parsedRoutine);
        
        Map<String, Object> routineResponse = new HashMap<>();
        routineResponse.put("id", "routine1");
        when(kraftLogApiClient.createRoutine(any())).thenReturn(routineResponse);
        
        Map<String, Object> workoutResponse = new HashMap<>();
        workoutResponse.put("id", "workout1");
        when(kraftLogApiClient.createWorkout(any())).thenReturn(workoutResponse);
        
        ParsedExerciseData apiExercise = ParsedExerciseData.builder()
                .id("ex1")
                .name("Supino Reto")
                .muscleGroup("Chest")
                .build();
        when(kraftLogApiClient.searchExercises(anyString()))
                .thenReturn(Collections.singletonList(apiExercise));
        
        doNothing().when(kraftLogApiClient).addExerciseToWorkout(any());
        
        RoutineImportResult result = routineImportService.importRoutineFromXlsx(
                inputStream, fileName, userId);
        
        assertNotNull(result);
        assertEquals("test", result.getRoutineName());
        assertEquals("routine1", result.getRoutineId());
        assertEquals(1, result.getTotalWorkouts());
        assertEquals(1, result.getSuccessfulWorkouts());
        assertEquals(0, result.getFailedWorkouts());
        assertEquals(1, result.getTotalExercises());
        assertEquals(1, result.getSuccessfulExercises());
        assertEquals(0, result.getFailedExercises());
        assertTrue(result.getErrors().isEmpty());
        
        verify(kraftLogApiClient).createRoutine(any());
        verify(kraftLogApiClient).createWorkout(any());
        verify(kraftLogApiClient).addExerciseToWorkout(any());
    }

    @Test
    void testImportRoutineFromXlsx_RoutineCreationFails() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        String fileName = "test.xlsx";
        String userId = "user123";
        
        ParsedRoutineData parsedRoutine = createTestRoutine();
        when(xlsxParserService.parseRoutineFromXlsx(inputStream, fileName))
                .thenReturn(parsedRoutine);
        
        when(kraftLogApiClient.createRoutine(any()))
                .thenThrow(new IOException("API error"));
        
        assertThrows(IOException.class, () -> 
                routineImportService.importRoutineFromXlsx(inputStream, fileName, userId));
    }

    @Test
    void testImportRoutineFromXlsx_WorkoutCreationFails() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        String fileName = "test.xlsx";
        String userId = "user123";
        
        ParsedRoutineData parsedRoutine = createTestRoutine();
        when(xlsxParserService.parseRoutineFromXlsx(inputStream, fileName))
                .thenReturn(parsedRoutine);
        
        Map<String, Object> routineResponse = new HashMap<>();
        routineResponse.put("id", "routine1");
        when(kraftLogApiClient.createRoutine(any())).thenReturn(routineResponse);
        
        when(kraftLogApiClient.createWorkout(any()))
                .thenThrow(new IOException("API error"));
        
        RoutineImportResult result = routineImportService.importRoutineFromXlsx(
                inputStream, fileName, userId);
        
        assertNotNull(result);
        assertEquals(1, result.getFailedWorkouts());
        assertEquals(0, result.getSuccessfulWorkouts());
        assertFalse(result.getErrors().isEmpty());
    }

    @Test
    void testImportRoutineFromXlsx_ExerciseNotFoundCreatesNew() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        String fileName = "test.xlsx";
        String userId = "user123";
        
        ParsedRoutineData parsedRoutine = createTestRoutine();
        when(xlsxParserService.parseRoutineFromXlsx(inputStream, fileName))
                .thenReturn(parsedRoutine);
        
        Map<String, Object> routineResponse = new HashMap<>();
        routineResponse.put("id", "routine1");
        when(kraftLogApiClient.createRoutine(any())).thenReturn(routineResponse);
        
        Map<String, Object> workoutResponse = new HashMap<>();
        workoutResponse.put("id", "workout1");
        when(kraftLogApiClient.createWorkout(any())).thenReturn(workoutResponse);
        
        when(kraftLogApiClient.searchExercises(anyString()))
                .thenReturn(Collections.emptyList());
        
        ParsedExerciseData createdExercise = ParsedExerciseData.builder()
                .id("new-ex1")
                .name("Supino Reto")
                .muscleGroup("Other")
                .build();
        when(kraftLogApiClient.createExercise(any())).thenReturn(createdExercise);
        
        // muscleGroupMappingConfig is autowired and uses real configuration
        
        doNothing().when(kraftLogApiClient).addExerciseToWorkout(any());
        
        RoutineImportResult result = routineImportService.importRoutineFromXlsx(
                inputStream, fileName, userId);
        
        assertNotNull(result);
        assertEquals(1, result.getSuccessfulExercises());
        
        verify(kraftLogApiClient).createExercise(any());
        verify(kraftLogApiClient).addExerciseToWorkout(any());
    }

    @Test
    void testImportRoutineFromXlsx_AddExerciseToWorkoutFails() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        String fileName = "test.xlsx";
        String userId = "user123";
        
        ParsedRoutineData parsedRoutine = createTestRoutine();
        when(xlsxParserService.parseRoutineFromXlsx(inputStream, fileName))
                .thenReturn(parsedRoutine);
        
        Map<String, Object> routineResponse = new HashMap<>();
        routineResponse.put("id", "routine1");
        when(kraftLogApiClient.createRoutine(any())).thenReturn(routineResponse);
        
        Map<String, Object> workoutResponse = new HashMap<>();
        workoutResponse.put("id", "workout1");
        when(kraftLogApiClient.createWorkout(any())).thenReturn(workoutResponse);
        
        ParsedExerciseData apiExercise = ParsedExerciseData.builder()
                .id("ex1")
                .name("Supino Reto")
                .muscleGroup("Chest")
                .build();
        when(kraftLogApiClient.searchExercises(anyString()))
                .thenReturn(Collections.singletonList(apiExercise));
        
        doThrow(new IOException("API error"))
                .when(kraftLogApiClient).addExerciseToWorkout(any());
        
        RoutineImportResult result = routineImportService.importRoutineFromXlsx(
                inputStream, fileName, userId);
        
        assertNotNull(result);
        assertEquals(1, result.getFailedExercises());
        assertFalse(result.getErrors().isEmpty());
    }

    private ParsedRoutineData createTestRoutine() {
        ParsedWorkoutExerciseData exercise = ParsedWorkoutExerciseData.builder()
                .exerciseName("Supino Reto")
                .sets(3)
                .repetitions(12)
                .advancedTechnique("Drop set")
                .build();
        
        ParsedWorkoutData workout = ParsedWorkoutData.builder()
                .workoutName("Workout A")
                .exercises(Collections.singletonList(exercise))
                .minRestMinutes(1)
                .maxRestMinutes(2)
                .build();
        
        return ParsedRoutineData.builder()
                .routineName("test")
                .workouts(Collections.singletonList(workout))
                .build();
    }
}
