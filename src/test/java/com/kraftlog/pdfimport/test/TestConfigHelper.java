package com.kraftlog.pdfimport.test;

import com.kraftlog.pdfimport.config.MuscleGroupMappingConfig;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class TestConfigHelper {
    
    public static MuscleGroupMappingConfig createTestMuscleGroupConfig() {
        MuscleGroupMappingConfig config = new MuscleGroupMappingConfig();
        
        // Create test muscle group mappings
        Map<String, String> testMappings = new HashMap<>();
        testMappings.put("PEITO", "Chest");
        testMappings.put("COSTAS", "Back");
        testMappings.put("PERNAS", "Legs");
        testMappings.put("OMBROS", "Shoulders");
        testMappings.put("BÍCEPS", "Biceps");
        testMappings.put("TRÍCEPS", "Triceps");
        testMappings.put("ABDÔMEN", "Abs");
        
        // Use reflection to set the private field
        try {
            Field muscleGroupsField = MuscleGroupMappingConfig.class.getDeclaredField("muscleGroups");
            muscleGroupsField.setAccessible(true);
            muscleGroupsField.set(config, testMappings);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test config", e);
        }
        
        return config;
    }
    
    public static MuscleGroupMappingConfig createEmptyMuscleGroupConfig() {
        MuscleGroupMappingConfig config = new MuscleGroupMappingConfig();
        
        try {
            Field muscleGroupsField = MuscleGroupMappingConfig.class.getDeclaredField("muscleGroups");
            muscleGroupsField.setAccessible(true);
            muscleGroupsField.set(config, new HashMap<>());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test config", e);
        }
        
        return config;
    }
}
