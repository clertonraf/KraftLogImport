package com.kraftlog.pdfimport.service;

import com.kraftlog.pdfimport.config.MuscleGroupMappingConfig;
import com.kraftlog.pdfimport.dto.ParsedExerciseData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfParserService {

    private final MuscleGroupMappingConfig muscleGroupConfig;

    private static final Pattern URL_PATTERN = Pattern.compile(
            "https://(?:(?:www\\.)?youtube\\.com/watch\\?v=|youtu\\.be/)[A-Za-z0-9_-]+");

    public List<ParsedExerciseData> parseExercisesFromPdf(File pdfFile) throws IOException {
        log.info("Parsing exercises from PDF: {}", pdfFile.getName());
        
        List<ParsedExerciseData> exercises = new ArrayList<>();
        
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            exercises = parseExercisesFromText(text);
            
            log.info("Successfully parsed {} exercises from PDF", exercises.size());
        }
        
        return exercises;
    }

    private List<ParsedExerciseData> parseExercisesFromText(String text) {
        List<ParsedExerciseData> exercises = new ArrayList<>();
        
        String[] lines = text.split("\n");
        String currentMuscleGroup = null;
        
        for (String line : lines) {
            line = line.trim();
            
            String detectedMuscleGroup = detectMuscleGroup(line);
            if (detectedMuscleGroup != null) {
                currentMuscleGroup = detectedMuscleGroup;
                log.debug("Found muscle group: {}", currentMuscleGroup);
                continue;
            }
            
            if (line.isEmpty() || line.startsWith("EXERCÍCIO") || line.startsWith("VÍDEO")) {
                continue;
            }
            
            if (currentMuscleGroup != null) {
                ParsedExerciseData exercise = parseExerciseLine(line, currentMuscleGroup);
                if (exercise != null) {
                    exercises.add(exercise);
                    log.debug("Parsed exercise: {} - {}", exercise.getName(), exercise.getMuscleGroupPortuguese());
                }
            }
        }
        
        return exercises;
    }

    private String detectMuscleGroup(String line) {
        String upperLine = line.toUpperCase();
        
        Set<String> configuredHeaders = muscleGroupConfig.getMuscleGroupHeaders();
        
        for (String header : configuredHeaders) {
            if (upperLine.contains(header.toUpperCase())) {
                return header;
            }
        }
        
        return null;
    }

    private ParsedExerciseData parseExerciseLine(String line, String muscleGroup) {
        Matcher urlMatcher = URL_PATTERN.matcher(line);
        String videoUrl = null;
        String exerciseName;
        
        if (urlMatcher.find()) {
            videoUrl = urlMatcher.group();
            int urlStart = line.indexOf(videoUrl);
            exerciseName = line.substring(0, urlStart).trim();
            
            log.debug("Found URL: {}", videoUrl);
        } else {
            exerciseName = line.trim();
        }
        
        exerciseName = cleanExerciseName(exerciseName);
        
        if (exerciseName.isEmpty() || exerciseName.length() < 3) {
            return null;
        }
        
        if (exerciseName.contains("http") || exerciseName.contains("youtu")) {
            log.warn("Exercise name still contains URL fragments: {}", exerciseName);
            return null;
        }
        
        return ParsedExerciseData.builder()
                .name(exerciseName)
                .videoUrl(videoUrl)
                .muscleGroupPortuguese(muscleGroup)
                .build();
    }

    private String cleanExerciseName(String name) {
        name = name.replaceAll("\\s+", " ").trim();
        name = name.replaceAll("^[\\d.\\-]+\\s*", "");
        name = name.replaceAll("[|\\t]+", " ");
        
        return name.trim();
    }
}
