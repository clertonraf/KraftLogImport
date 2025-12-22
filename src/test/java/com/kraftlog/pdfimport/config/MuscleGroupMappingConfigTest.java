package com.kraftlog.pdfimport.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MuscleGroupMappingConfigTest {

    private MuscleGroupMappingConfig config;

    @BeforeEach
    void setUp() {
        config = new MuscleGroupMappingConfig();
    }

    @Test
    void testLoadConfigurationWithValidFile(@TempDir Path tempDir) throws IOException {
        Path configFile = tempDir.resolve("test-muscle-groups.yml");
        Files.writeString(configFile, 
                "PEITO: Chest\n" +
                "COSTAS: Back\n" +
                "PERNAS: Legs\n");

        config.setConfigPath(configFile.toString());
        config.loadConfiguration();

        assertTrue(config.hasConfiguration());
        assertEquals(3, config.getMuscleGroupMapping().size());
        assertEquals("Chest", config.getMuscleGroupEnglishName("PEITO"));
        assertEquals("Back", config.getMuscleGroupEnglishName("COSTAS"));
        assertEquals("Legs", config.getMuscleGroupEnglishName("PERNAS"));
    }

    @Test
    void testLoadConfigurationWithNonExistentFile() {
        config.setConfigPath("/non/existent/file.yml");
        config.loadConfiguration();

        assertFalse(config.hasConfiguration());
        assertTrue(config.getMuscleGroupMapping().isEmpty());
    }

    @Test
    void testLoadConfigurationWithNullPath() {
        config.setConfigPath(null);
        config.loadConfiguration();

        assertFalse(config.hasConfiguration());
        assertTrue(config.getMuscleGroupMapping().isEmpty());
    }

    @Test
    void testLoadConfigurationWithEmptyPath() {
        config.setConfigPath("   ");
        config.loadConfiguration();

        assertFalse(config.hasConfiguration());
        assertTrue(config.getMuscleGroupMapping().isEmpty());
    }

    @Test
    void testLoadConfigurationWithEmptyFile(@TempDir Path tempDir) throws IOException {
        Path configFile = tempDir.resolve("empty.yml");
        Files.writeString(configFile, "");

        config.setConfigPath(configFile.toString());
        config.loadConfiguration();

        assertFalse(config.hasConfiguration());
        assertTrue(config.getMuscleGroupMapping().isEmpty());
    }

    @Test
    void testGetMuscleGroupHeaders(@TempDir Path tempDir) throws IOException {
        Path configFile = tempDir.resolve("headers.yml");
        Files.writeString(configFile, 
                "PEITO: Chest\n" +
                "COSTAS: Back\n");

        config.setConfigPath(configFile.toString());
        config.loadConfiguration();

        Set<String> headers = config.getMuscleGroupHeaders();
        assertEquals(2, headers.size());
        assertTrue(headers.contains("PEITO"));
        assertTrue(headers.contains("COSTAS"));
    }

    @Test
    void testGetMuscleGroupEnglishNameCaseInsensitive(@TempDir Path tempDir) throws IOException {
        Path configFile = tempDir.resolve("case-test.yml");
        Files.writeString(configFile, "PEITO: Chest\n");

        config.setConfigPath(configFile.toString());
        config.loadConfiguration();

        assertEquals("Chest", config.getMuscleGroupEnglishName("PEITO"));
        assertEquals("Chest", config.getMuscleGroupEnglishName("peito"));
        assertEquals("Chest", config.getMuscleGroupEnglishName("Peito"));
    }

    @Test
    void testGetMuscleGroupEnglishNameWithNull() {
        assertNull(config.getMuscleGroupEnglishName(null));
    }

    @Test
    void testGetMuscleGroupEnglishNameNotFound(@TempDir Path tempDir) throws IOException {
        Path configFile = tempDir.resolve("test.yml");
        Files.writeString(configFile, "PEITO: Chest\n");

        config.setConfigPath(configFile.toString());
        config.loadConfiguration();

        assertNull(config.getMuscleGroupEnglishName("UNKNOWN"));
    }

    @Test
    void testHasConfigurationInitiallyFalse() {
        assertFalse(config.hasConfiguration());
    }

    @Test
    void testLoadConfigurationWithComplexYaml(@TempDir Path tempDir) throws IOException {
        Path configFile = tempDir.resolve("complex.yml");
        Files.writeString(configFile, 
                "PEITO: Chest\n" +
                "COSTAS: Back\n" +
                "PERNAS: Legs\n" +
                "OMBROS: Shoulders\n" +
                "BÍCEPS: Biceps\n" +
                "TRÍCEPS: Triceps\n" +
                "ABDÔMEN: Abs\n");

        config.setConfigPath(configFile.toString());
        config.loadConfiguration();

        assertTrue(config.hasConfiguration());
        assertEquals(7, config.getMuscleGroupMapping().size());
        assertEquals("Abs", config.getMuscleGroupEnglishName("ABDÔMEN"));
    }

    @Test
    void testLoadConfigurationWithNullValues(@TempDir Path tempDir) throws IOException {
        Path configFile = tempDir.resolve("null-values.yml");
        Files.writeString(configFile, 
                "PEITO: Chest\n" +
                "COSTAS:\n" +
                "PERNAS: Legs\n");

        config.setConfigPath(configFile.toString());
        config.loadConfiguration();

        assertEquals(2, config.getMuscleGroupMapping().size());
        assertEquals("Chest", config.getMuscleGroupEnglishName("PEITO"));
        assertEquals("Legs", config.getMuscleGroupEnglishName("PERNAS"));
        assertNull(config.getMuscleGroupEnglishName("COSTAS"));
    }

    @Test
    void testGetMuscleGroupHeadersEmptyWhenNoConfig() {
        Set<String> headers = config.getMuscleGroupHeaders();
        assertNotNull(headers);
        assertTrue(headers.isEmpty());
    }
}
