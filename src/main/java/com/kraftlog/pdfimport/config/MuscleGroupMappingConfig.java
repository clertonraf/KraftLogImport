package com.kraftlog.pdfimport.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.yaml.snakeyaml.Yaml;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Configuration
@Data
@Slf4j
public class MuscleGroupMappingConfig {

    @Value("${kraftlog.muscle-groups.config-path:exercise-muscle-groups.yml}")
    private String configPath;

    private Map<String, String> muscleGroupMapping = new HashMap<>();

    @PostConstruct
    public void loadConfiguration() {
        if (configPath == null || configPath.trim().isEmpty()) {
            log.warn("No muscle groups configuration file specified. " +
                    "Set 'kraftlog.muscle-groups.config-path' or environment variable 'EXERCISE_MUSCLE_GROUPS_CONFIG_PATH'. " +
                    "Exercises will be imported without muscle group associations.");
            return;
        }

        try {
            log.info("Loading muscle groups configuration from: {}", configPath);
            
            try (InputStream inputStream = new FileInputStream(configPath)) {
                Yaml yaml = new Yaml();
                Map<String, Object> data = yaml.load(inputStream);
                
                if (data != null) {
                    data.forEach((key, value) -> {
                        if (value != null) {
                            muscleGroupMapping.put(key, value.toString());
                        }
                    });
                    
                    log.info("Successfully loaded {} muscle group mappings from configuration file", 
                            muscleGroupMapping.size());
                    
                    if (log.isDebugEnabled()) {
                        muscleGroupMapping.forEach((pt, en) -> 
                            log.debug("  Mapping: {} -> {}", pt, en));
                    }
                } else {
                    log.warn("Configuration file is empty or invalid: {}", configPath);
                }
            }
            
        } catch (IOException e) {
            log.warn("Could not load muscle groups configuration from '{}': {}. " +
                    "Exercises will be imported without muscle group associations.", 
                    configPath, e.getMessage());
        }
    }

    public boolean hasConfiguration() {
        return !muscleGroupMapping.isEmpty();
    }

    public Set<String> getMuscleGroupHeaders() {
        return muscleGroupMapping.keySet();
    }

    public Map<String, String> getMuscleGroupMappings() {
        return new HashMap<>(muscleGroupMapping);
    }

    public String getMuscleGroupEnglishName(String portugueseName) {
        if (portugueseName == null) {
            return null;
        }
        return muscleGroupMapping.get(portugueseName.toUpperCase());
    }
}
