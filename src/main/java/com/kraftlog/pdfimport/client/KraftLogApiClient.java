package com.kraftlog.pdfimport.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kraftlog.pdfimport.config.KraftLogApiProperties;
import com.kraftlog.pdfimport.dto.ExerciseCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Slf4j
public class KraftLogApiClient {

    private final KraftLogApiProperties apiProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    
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

    public boolean createExercise(ExerciseCreateRequest exercise) throws IOException, InterruptedException {
        if (authToken == null || authToken.isEmpty()) {
            authenticate();
        }
        
        String createUrl = apiProperties.getBaseUrl() + "/api/exercises";
        String exerciseJson = objectMapper.writeValueAsString(exercise);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(createUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .POST(HttpRequest.BodyPublishers.ofString(exerciseJson))
                .build();
        
        log.debug("Creating exercise: {}", exercise.getName());
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 201) {
            log.info("Successfully created exercise: {}", exercise.getName());
            return true;
        } else if (response.statusCode() == 401) {
            log.warn("Token expired, re-authenticating...");
            authenticate();
            return createExercise(exercise);
        } else {
            log.error("Failed to create exercise '{}': {} - {}", 
                    exercise.getName(), response.statusCode(), response.body());
            return false;
        }
    }
}
