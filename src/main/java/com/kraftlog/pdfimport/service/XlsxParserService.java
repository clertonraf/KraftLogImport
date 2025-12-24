package com.kraftlog.pdfimport.service;

import com.kraftlog.pdfimport.dto.ParsedRoutineData;
import com.kraftlog.pdfimport.dto.ParsedWorkoutData;
import com.kraftlog.pdfimport.dto.ParsedWorkoutExerciseData;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class XlsxParserService {

    private static final Pattern REST_PATTERN = Pattern.compile("(\\d+)\\s*a\\s*(\\d+)\\s*minutos?");
    private static final Pattern SETS_REPS_PATTERN = Pattern.compile("(\\d+)\\s*[xX]\\s*(\\d+)");

    public ParsedRoutineData parseRoutineFromXlsx(InputStream inputStream, String fileName) throws IOException {
        log.info("Parsing routine from XLSX: {}", fileName);
        
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            List<ParsedWorkoutData> workouts = new ArrayList<>();
            
            // Parse 5 workouts based on the structure described (using 0-based row indices)
            // colOffset, nameRow, firstExerciseRow, lastExerciseRow (inclusive), restRow
            workouts.add(parseWorkout(sheet, 1, 1, 3, 7, 15));   // Workout 1: B2 name, B4-B8 exercises, B16 rest
            workouts.add(parseWorkout(sheet, 5, 1, 3, 9, 15));  // Workout 2: F2 name, F4-F10 exercises, F16 rest
            workouts.add(parseWorkout(sheet, 9, 1, 3, 9, 15));  // Workout 3: J2 name, J4-J10 exercises, J16 rest
            workouts.add(parseWorkout(sheet, 1, 17, 19, 24, 15)); // Workout 4: B18 name, B20-B25 exercises, B16 rest
            workouts.add(parseWorkout(sheet, 5, 17, 19, 26, 15)); // Workout 5: F18 name, F20-F27 exercises, F16 rest
            
            return ParsedRoutineData.builder()
                    .routineName(fileName.replace(".xlsx", ""))
                    .workouts(workouts)
                    .build();
        } catch (Exception e) {
            log.error("Error parsing XLSX file: {}", fileName, e);
            throw new IOException("Failed to parse XLSX file: " + fileName, e);
        }
    }

    private ParsedWorkoutData parseWorkout(Sheet sheet, int colOffset, int nameRow, int firstExerciseRow, 
                                           int lastExerciseRow, int restRow) {
        // Parse workout name
        Row workoutNameRow = sheet.getRow(nameRow);
        String workoutName = getCellValue(workoutNameRow, colOffset);
        
        log.debug("Parsing workout: {} at column offset {} (rows {}-{})", workoutName, colOffset, firstExerciseRow, lastExerciseRow);
        
        // Parse rest intervals
        Row restIntervalRow = sheet.getRow(restRow);
        String restInterval = getCellValue(restIntervalRow, colOffset);
        Integer minRest = null;
        Integer maxRest = null;
        
        if (restInterval != null && !restInterval.isEmpty()) {
            Matcher matcher = REST_PATTERN.matcher(restInterval);
            if (matcher.find()) {
                minRest = Integer.parseInt(matcher.group(1));
                maxRest = Integer.parseInt(matcher.group(2));
            }
        }
        
        // Parse exercises
        List<ParsedWorkoutExerciseData> exercises = new ArrayList<>();
        
        for (int exerciseRow = firstExerciseRow; exerciseRow <= lastExerciseRow; exerciseRow++) {
            Row row = sheet.getRow(exerciseRow);
            if (row == null) {
                continue;
            }
            
            String exerciseName = getCellValue(row, colOffset);
            if (exerciseName == null || exerciseName.trim().isEmpty()) {
                continue;
            }
            
            String setsReps = getCellValue(row, colOffset + 1);
            String advancedTechnique = getCellValue(row, colOffset + 2);
            
            Integer sets = null;
            Integer reps = null;
            
            if (setsReps != null && !setsReps.isEmpty()) {
                Matcher matcher = SETS_REPS_PATTERN.matcher(setsReps);
                if (matcher.find()) {
                    sets = Integer.parseInt(matcher.group(1));
                    reps = Integer.parseInt(matcher.group(2));
                }
            }
            
            exercises.add(ParsedWorkoutExerciseData.builder()
                    .exerciseName(exerciseName.trim())
                    .sets(sets)
                    .repetitions(reps)
                    .advancedTechnique(advancedTechnique != null && !advancedTechnique.trim().isEmpty() ? 
                            advancedTechnique.trim() : null)
                    .build());
        }
        
        log.debug("Parsed {} exercises for workout: {}", exercises.size(), workoutName);
        
        return ParsedWorkoutData.builder()
                .workoutName(workoutName != null ? workoutName.trim() : "Unnamed Workout")
                .exercises(exercises)
                .minRestMinutes(minRest)
                .maxRestMinutes(maxRest)
                .build();
    }

    private String getCellValue(Row row, int cellIndex) {
        if (row == null) {
            return null;
        }
        
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return null;
        }
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }
}
