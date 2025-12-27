package com.kraftlog.pdfimport.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kraftlog.pdfimport.client.KraftLogApiClient;
import com.kraftlog.pdfimport.config.MuscleGroupMappingConfig;
import com.kraftlog.pdfimport.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutineImportService {

    private final XlsxParserService xlsxParserService;
    private final KraftLogApiClient kraftLogApiClient;
    private final MuscleGroupMappingConfig muscleGroupMappingConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parse routine from XLSX and generate JSON structure
     */
    public String generateRoutineJson(InputStream xlsxInputStream, String fileName) throws IOException {
        log.info("Generating routine JSON from XLSX: {}", fileName);
        
        ParsedRoutineData parsedRoutine = xlsxParserService.parseRoutineFromXlsx(xlsxInputStream, fileName);
        
        // Build routine structure matching KraftLog API
        Map<String, Object> routine = new LinkedHashMap<>();
        routine.put("name", parsedRoutine.getRoutineName());
        
        List<Map<String, Object>> workouts = new ArrayList<>();
        int workoutOrder = 1;
        
        for (ParsedWorkoutData workout : parsedRoutine.getWorkouts()) {
            Map<String, Object> workoutData = new LinkedHashMap<>();
            workoutData.put("name", workout.getWorkoutName());
            workoutData.put("order", workoutOrder++);
            
            // Add rest intervals if present
            if (workout.getMinRestMinutes() != null && workout.getMaxRestMinutes() != null) {
                workoutData.put("minRestSeconds", workout.getMinRestMinutes() * 60);
                workoutData.put("maxRestSeconds", workout.getMaxRestMinutes() * 60);
            }
            
            List<Map<String, Object>> exercises = new ArrayList<>();
            int exerciseOrder = 1;
            
            for (ParsedWorkoutExerciseData exercise : workout.getExercises()) {
                Map<String, Object> exerciseData = new LinkedHashMap<>();
                exerciseData.put("name", exercise.getExerciseName());
                exerciseData.put("order", exerciseOrder++);
                
                // Try to find exercise in API to get muscle group
                try {
                    List<ParsedExerciseData> apiExercises = kraftLogApiClient.searchExercises(exercise.getExerciseName());
                    if (!apiExercises.isEmpty()) {
                        ParsedExerciseData apiExercise = apiExercises.get(0);
                        exerciseData.put("muscleGroup", apiExercise.getMuscleGroup());
                        log.debug("Found exercise '{}' with muscle group: {}", 
                                exercise.getExerciseName(), apiExercise.getMuscleGroup());
                    } else {
                        log.warn("Exercise '{}' not found in KraftLog API", exercise.getExerciseName());
                    }
                } catch (Exception e) {
                    log.warn("Failed to search exercise '{}' in API: {}", 
                            exercise.getExerciseName(), e.getMessage());
                }
                
                if (exercise.getSets() != null) {
                    exerciseData.put("sets", exercise.getSets());
                }
                if (exercise.getRepetitions() != null) {
                    exerciseData.put("repetitions", exercise.getRepetitions());
                }
                if (exercise.getAdvancedTechnique() != null) {
                    exerciseData.put("advancedTechnique", exercise.getAdvancedTechnique());
                }
                
                exercises.add(exerciseData);
            }
            
            workoutData.put("exercises", exercises);
            workouts.add(workoutData);
        }
        
        routine.put("workouts", workouts);
        
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(routine);
        log.info("Generated routine JSON with {} workouts", workouts.size());
        
        return json;
    }

    /**
     * Import routine from XLSX to KraftLog API
     */
    public RoutineImportResult importRoutineFromXlsx(InputStream xlsxInputStream, String fileName, String userId) 
            throws IOException {
        log.info("Starting routine import from XLSX: {} for user: {}", fileName, userId);
        
        ParsedRoutineData parsedRoutine = xlsxParserService.parseRoutineFromXlsx(xlsxInputStream, fileName);
        
        int totalWorkouts = parsedRoutine.getWorkouts().size();
        int successfulWorkouts = 0;
        int failedWorkouts = 0;
        int totalExercises = 0;
        int successfulExercises = 0;
        int failedExercises = 0;
        
        List<String> errors = new ArrayList<>();
        
        // First, create or get the routine
        String routineId = createRoutine(parsedRoutine.getRoutineName(), userId);
        if (routineId == null) {
            throw new IOException("Failed to create routine in KraftLog API");
        }
        
        // Import each workout
        for (ParsedWorkoutData workout : parsedRoutine.getWorkouts()) {
            try {
                log.debug("Importing workout: {}", workout.getWorkoutName());
                
                String workoutId = createWorkout(routineId, workout, userId);
                if (workoutId == null) {
                    failedWorkouts++;
                    errors.add("Failed to create workout: " + workout.getWorkoutName());
                    continue;
                }
                
                successfulWorkouts++;
                
                // Import exercises for this workout
                for (ParsedWorkoutExerciseData exercise : workout.getExercises()) {
                    totalExercises++;
                    try {
                        // Find exercise in API
                        List<ParsedExerciseData> apiExercises = 
                                kraftLogApiClient.searchExercises(exercise.getExerciseName());
                        
                        String exerciseId = null;
                        String muscleGroup = null;
                        
                        if (!apiExercises.isEmpty()) {
                            ParsedExerciseData apiExercise = apiExercises.get(0);
                            exerciseId = apiExercise.getId();
                            muscleGroup = apiExercise.getMuscleGroup();
                        } else {
                            // Create exercise if not found
                            log.debug("Exercise '{}' not found, creating it", exercise.getExerciseName());
                            muscleGroup = determineMuscleGroup(exercise.getExerciseName());
                            exerciseId = createExercise(exercise.getExerciseName(), muscleGroup, userId);
                        }
                        
                        if (exerciseId == null) {
                            failedExercises++;
                            errors.add("Failed to find or create exercise: " + exercise.getExerciseName());
                            continue;
                        }
                        
                        // Add exercise to workout
                        boolean added = addExerciseToWorkout(workoutId, exerciseId, exercise, userId);
                        if (added) {
                            successfulExercises++;
                        } else {
                            failedExercises++;
                            errors.add("Failed to add exercise to workout: " + exercise.getExerciseName());
                        }
                        
                    } catch (Exception e) {
                        failedExercises++;
                        String errorMsg = String.format("Failed to import exercise '%s': %s", 
                                exercise.getExerciseName(), e.getMessage());
                        errors.add(errorMsg);
                        log.warn(errorMsg);
                    }
                }
                
            } catch (Exception e) {
                failedWorkouts++;
                String errorMsg = String.format("Failed to import workout '%s': %s", 
                        workout.getWorkoutName(), e.getMessage());
                errors.add(errorMsg);
                log.warn(errorMsg);
            }
        }
        
        log.info("Routine import completed. Workouts: {}/{} successful. Exercises: {}/{} successful", 
                successfulWorkouts, totalWorkouts, successfulExercises, totalExercises);
        
        return RoutineImportResult.builder()
                .routineName(parsedRoutine.getRoutineName())
                .routineId(routineId)
                .totalWorkouts(totalWorkouts)
                .successfulWorkouts(successfulWorkouts)
                .failedWorkouts(failedWorkouts)
                .totalExercises(totalExercises)
                .successfulExercises(successfulExercises)
                .failedExercises(failedExercises)
                .errors(errors)
                .build();
    }

    private String createRoutine(String routineName, String userId) {
        try {
            Map<String, Object> routineData = new LinkedHashMap<>();
            routineData.put("name", routineName);
            routineData.put("userId", userId);
            
            Map<String, Object> response = kraftLogApiClient.createRoutine(routineData);
            return (String) response.get("id");
        } catch (Exception e) {
            log.error("Failed to create routine: {}", routineName, e);
            return null;
        }
    }

    private String createWorkout(String routineId, ParsedWorkoutData workout, String userId) {
        try {
            Map<String, Object> workoutData = new LinkedHashMap<>();
            workoutData.put("name", workout.getWorkoutName());
            workoutData.put("routineId", routineId);
            workoutData.put("userId", userId);
            
            if (workout.getMinRestMinutes() != null) {
                workoutData.put("minRestSeconds", workout.getMinRestMinutes() * 60);
            }
            if (workout.getMaxRestMinutes() != null) {
                workoutData.put("maxRestSeconds", workout.getMaxRestMinutes() * 60);
            }
            
            Map<String, Object> response = kraftLogApiClient.createWorkout(workoutData);
            return (String) response.get("id");
        } catch (Exception e) {
            log.error("Failed to create workout: {}", workout.getWorkoutName(), e);
            return null;
        }
    }

    private String createExercise(String exerciseName, String muscleGroup, String userId) {
        try {
            ExerciseCreateRequest request = new ExerciseCreateRequest();
            request.setName(exerciseName);
            request.setMuscleGroup(muscleGroup);
            
            ParsedExerciseData created = kraftLogApiClient.createExercise(request);
            return created.getId();
        } catch (Exception e) {
            log.error("Failed to create exercise: {}", exerciseName, e);
            return null;
        }
    }

    private boolean addExerciseToWorkout(String workoutId, String exerciseId, 
                                         ParsedWorkoutExerciseData exerciseData, String userId) {
        try {
            Map<String, Object> workoutExercise = new LinkedHashMap<>();
            workoutExercise.put("workoutId", workoutId);
            workoutExercise.put("exerciseId", exerciseId);
            workoutExercise.put("userId", userId);
            
            if (exerciseData.getSets() != null) {
                workoutExercise.put("recommendedSets", exerciseData.getSets());
            }
            if (exerciseData.getRepetitions() != null) {
                workoutExercise.put("recommendedReps", exerciseData.getRepetitions());
            }
            if (exerciseData.getAdvancedTechnique() != null) {
                workoutExercise.put("trainingTechnique", exerciseData.getAdvancedTechnique());
            }
            
            kraftLogApiClient.addExerciseToWorkout(workoutExercise);
            return true;
        } catch (Exception e) {
            log.error("Failed to add exercise {} to workout {}", exerciseId, workoutId, e);
            return false;
        }
    }

    private String determineMuscleGroup(String exerciseName) {
        // Try to determine muscle group from exercise name using config mappings
        Map<String, String> mappings = muscleGroupMappingConfig.getMuscleGroupMappings();
        
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            if (exerciseName.toUpperCase().contains(entry.getKey().toUpperCase())) {
                return entry.getValue();
            }
        }
        
        // Default to "Other" if no match found
        return "Other";
    }
}
