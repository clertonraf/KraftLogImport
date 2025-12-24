package com.kraftlog.pdfimport.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kraftlog.pdfimport.config.KraftLogApiProperties;
import com.kraftlog.pdfimport.dto.ExerciseCreateRequest;
import com.kraftlog.pdfimport.dto.ParsedExerciseData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KraftLogApiClient {

    private final KraftLogApiProperties apiProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .build();
    
    private String authToken;

    public void authenticate() throws IOException, InterruptedException {
        String loginUrl = apiProperties.getBaseUrl() + "/api/auth/login";
        
        String loginJson = String.format(
            "{\"username\":\"%s\",\"password\":\"%s\"}",
            apiProperties.getAuth().getUsername(),
            apiProperties.getAuth().getPassword()
        );
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(loginUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(loginJson))
                .build();
        
        log.info("Authenticating with KraftLog API at {}", loginUrl);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            var loginResponse = objectMapper.readTree(response.body());
            authToken = loginResponse.get("token").asText();
            log.info("Successfully authenticated with KraftLog API");
        } else {
            throw new IOException("Authentication failed with status: " + response.statusCode() + " - " + response.body());
        }
    }

    public ParsedExerciseData createExercise(ExerciseCreateRequest exercise) throws IOException, InterruptedException {
        return createExercise(exercise, 0);
    }
    
    private ParsedExerciseData createExercise(ExerciseCreateRequest exercise, int retryCount) throws IOException, InterruptedException {
        if (retryCount > 1) {
            throw new IOException("Maximum retry attempts exceeded for exercise: " + exercise.getName());
        }
        
        if (authToken == null || authToken.isEmpty()) {
            authenticate();
        }
        
        String createUrl = apiProperties.getBaseUrl() + "/api/exercises";
        String exerciseJson = objectMapper.writeValueAsString(exercise);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(createUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .timeout(java.time.Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(exerciseJson))
                .build();
        
        log.debug("Creating exercise: {}", exercise.getName());
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 201) {
            log.info("Successfully created exercise: {}", exercise.getName());
            return objectMapper.readValue(response.body(), ParsedExerciseData.class);
        } else if (response.statusCode() == 401 && retryCount == 0) {
            log.warn("Token expired, re-authenticating...");
            authenticate();
            return createExercise(exercise, retryCount + 1);
        } else {
            log.error("Failed to create exercise '{}': {} - {}", 
                    exercise.getName(), response.statusCode(), response.body());
            throw new IOException("Failed to create exercise: " + exercise.getName());
        }
    }

    public List<ParsedExerciseData> searchExercises(String searchTerm) throws IOException, InterruptedException {
        if (authToken == null || authToken.isEmpty()) {
            authenticate();
        }
        
        String encodedSearch = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8);
        String searchUrl = apiProperties.getBaseUrl() + "/api/exercises?search=" + encodedSearch;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(searchUrl))
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();
        
        log.debug("Searching exercises: {}", searchTerm);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            TypeReference<List<ParsedExerciseData>> typeRef = new TypeReference<>() {};
            return objectMapper.readValue(response.body(), typeRef);
        } else if (response.statusCode() == 401) {
            log.warn("Token expired, re-authenticating...");
            authenticate();
            return searchExercises(searchTerm);
        } else {
            log.error("Failed to search exercises: {} - {}", response.statusCode(), response.body());
            throw new IOException("Failed to search exercises");
        }
    }

    public Map<String, Object> createRoutine(Map<String, Object> routineData) throws IOException, InterruptedException {
        if (authToken == null || authToken.isEmpty()) {
            authenticate();
        }
        
        String createUrl = apiProperties.getBaseUrl() + "/api/routines";
        String routineJson = objectMapper.writeValueAsString(routineData);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(createUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .POST(HttpRequest.BodyPublishers.ofString(routineJson))
                .build();
        
        log.debug("Creating routine: {}", routineData.get("name"));
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 201) {
            log.info("Successfully created routine: {}", routineData.get("name"));
            TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
            return objectMapper.readValue(response.body(), typeRef);
        } else if (response.statusCode() == 401) {
            log.warn("Token expired, re-authenticating...");
            authenticate();
            return createRoutine(routineData);
        } else {
            log.error("Failed to create routine: {} - {}", response.statusCode(), response.body());
            throw new IOException("Failed to create routine");
        }
    }

    public Map<String, Object> createWorkout(Map<String, Object> workoutData) throws IOException, InterruptedException {
        if (authToken == null || authToken.isEmpty()) {
            authenticate();
        }
        
        String createUrl = apiProperties.getBaseUrl() + "/api/workouts";
        String workoutJson = objectMapper.writeValueAsString(workoutData);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(createUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .POST(HttpRequest.BodyPublishers.ofString(workoutJson))
                .build();
        
        log.debug("Creating workout: {}", workoutData.get("name"));
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 201) {
            log.info("Successfully created workout: {}", workoutData.get("name"));
            TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
            return objectMapper.readValue(response.body(), typeRef);
        } else if (response.statusCode() == 401) {
            log.warn("Token expired, re-authenticating...");
            authenticate();
            return createWorkout(workoutData);
        } else {
            log.error("Failed to create workout: {} - {}", response.statusCode(), response.body());
            throw new IOException("Failed to create workout");
        }
    }

    public void addExerciseToWorkout(Map<String, Object> workoutExerciseData) throws IOException, InterruptedException {
        if (authToken == null || authToken.isEmpty()) {
            authenticate();
        }
        
        String createUrl = apiProperties.getBaseUrl() + "/api/workout-exercises";
        String json = objectMapper.writeValueAsString(workoutExerciseData);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(createUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        
        log.debug("Adding exercise to workout");
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 201) {
            log.info("Successfully added exercise to workout");
        } else if (response.statusCode() == 401) {
            log.warn("Token expired, re-authenticating...");
            authenticate();
            addExerciseToWorkout(workoutExerciseData);
        } else {
            log.error("Failed to add exercise to workout: {} - {}", response.statusCode(), response.body());
            throw new IOException("Failed to add exercise to workout");
        }
    }
}
