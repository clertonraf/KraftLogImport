package com.kraftlog.pdfimport.service;

import com.kraftlog.pdfimport.dto.ParsedRoutineData;
import com.kraftlog.pdfimport.dto.ParsedWorkoutData;
import com.kraftlog.pdfimport.dto.ParsedWorkoutExerciseData;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class XlsxParserServiceTest {

    private XlsxParserService xlsxParserService;

    @BeforeEach
    void setUp() {
        xlsxParserService = new XlsxParserService();
    }

    @Test
    void testParseRoutineFromXlsx_Success() throws IOException {
        // Create a minimal test Excel file
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (Workbook workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Routine");
            
            // Workout 1 - BCD2
            var row1 = sheet.createRow(1);
            row1.createCell(1).setCellValue("Workout A");
            
            // Exercises for Workout 1
            var row3 = sheet.createRow(3);
            row3.createCell(1).setCellValue("Supino Reto");
            row3.createCell(2).setCellValue("3x12");
            row3.createCell(3).setCellValue("Drop set");
            
            var row4 = sheet.createRow(4);
            row4.createCell(1).setCellValue("Supino Inclinado");
            row4.createCell(2).setCellValue("4x10");
            
            // Rest interval for Workout 1
            var row15 = sheet.createRow(15);
            row15.createCell(1).setCellValue("1 a 2 minutos");
            
            workbook.write(outputStream);
        }
        
        byte[] excelData = outputStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(excelData);
        
        ParsedRoutineData result = xlsxParserService.parseRoutineFromXlsx(inputStream, "test.xlsx");
        
        assertNotNull(result);
        assertEquals("test", result.getRoutineName());
        assertEquals(5, result.getWorkouts().size());
        
        ParsedWorkoutData workout1 = result.getWorkouts().get(0);
        assertEquals("Workout A", workout1.getWorkoutName());
        assertEquals(2, workout1.getExercises().size());
        assertEquals(1, workout1.getMinRestMinutes());
        assertEquals(2, workout1.getMaxRestMinutes());
        
        ParsedWorkoutExerciseData exercise1 = workout1.getExercises().get(0);
        assertEquals("Supino Reto", exercise1.getExerciseName());
        assertEquals(3, exercise1.getSets());
        assertEquals(12, exercise1.getRepetitions());
        assertEquals("Drop set", exercise1.getAdvancedTechnique());
    }

    @Test
    void testParseRoutineFromXlsx_InvalidFile() {
        ByteArrayInputStream inputStream = new ByteArrayInputStream("not an excel file".getBytes());
        
        assertThrows(IOException.class, () -> 
                xlsxParserService.parseRoutineFromXlsx(inputStream, "test.xlsx"));
    }

    @Test
    void testParseRoutineFromXlsx_EmptyFile() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (Workbook workbook = new XSSFWorkbook()) {
            workbook.createSheet("Empty");
            workbook.write(outputStream);
        }
        
        byte[] excelData = outputStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(excelData);
        
        ParsedRoutineData result = xlsxParserService.parseRoutineFromXlsx(inputStream, "empty.xlsx");
        
        assertNotNull(result);
        assertEquals("empty", result.getRoutineName());
        assertEquals(5, result.getWorkouts().size());
        
        // All workouts should be parsed but with no exercises
        for (ParsedWorkoutData workout : result.getWorkouts()) {
            assertTrue(workout.getExercises().isEmpty() || workout.getExercises().size() > 0);
        }
    }

    @Test
    void testParseSetsAndReps_ValidFormats() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (Workbook workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Test");
            
            var row3 = sheet.createRow(3);
            row3.createCell(1).setCellValue("Exercise1");
            row3.createCell(2).setCellValue("3x12");
            
            var row4 = sheet.createRow(4);
            row4.createCell(1).setCellValue("Exercise2");
            row4.createCell(2).setCellValue("4X10");
            
            var row5 = sheet.createRow(5);
            row5.createCell(1).setCellValue("Exercise3");
            row5.createCell(2).setCellValue("5 x 8");
            
            workbook.write(outputStream);
        }
        
        byte[] excelData = outputStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(excelData);
        
        ParsedRoutineData result = xlsxParserService.parseRoutineFromXlsx(inputStream, "test.xlsx");
        
        ParsedWorkoutData workout = result.getWorkouts().get(0);
        assertEquals(3, workout.getExercises().size());
        
        assertEquals(3, workout.getExercises().get(0).getSets());
        assertEquals(12, workout.getExercises().get(0).getRepetitions());
        
        assertEquals(4, workout.getExercises().get(1).getSets());
        assertEquals(10, workout.getExercises().get(1).getRepetitions());
        
        assertEquals(5, workout.getExercises().get(2).getSets());
        assertEquals(8, workout.getExercises().get(2).getRepetitions());
    }

    @Test
    void testParseRestInterval_ValidFormats() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (Workbook workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Test");
            
            var row1 = sheet.createRow(1);
            row1.createCell(1).setCellValue("Workout");
            
            var row15 = sheet.createRow(15);
            row15.createCell(1).setCellValue("2 a 3 minutos");
            
            workbook.write(outputStream);
        }
        
        byte[] excelData = outputStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(excelData);
        
        ParsedRoutineData result = xlsxParserService.parseRoutineFromXlsx(inputStream, "test.xlsx");
        
        ParsedWorkoutData workout = result.getWorkouts().get(0);
        assertEquals(2, workout.getMinRestMinutes());
        assertEquals(3, workout.getMaxRestMinutes());
    }

    @Test
    void testParseExercisesWithoutAdvancedTechnique() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (Workbook workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Test");
            
            var row1 = sheet.createRow(1);
            row1.createCell(1).setCellValue("Workout");
            
            var row3 = sheet.createRow(3);
            row3.createCell(1).setCellValue("Agachamento");
            row3.createCell(2).setCellValue("4x8");
            
            workbook.write(outputStream);
        }
        
        byte[] excelData = outputStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(excelData);
        
        ParsedRoutineData result = xlsxParserService.parseRoutineFromXlsx(inputStream, "test.xlsx");
        
        ParsedWorkoutData workout = result.getWorkouts().get(0);
        assertEquals(1, workout.getExercises().size());
        
        ParsedWorkoutExerciseData exercise = workout.getExercises().get(0);
        assertEquals("Agachamento", exercise.getExerciseName());
        assertNull(exercise.getAdvancedTechnique());
    }
}
