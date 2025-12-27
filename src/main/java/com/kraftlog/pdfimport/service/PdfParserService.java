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
        List<String> exerciseNames = new ArrayList<>();
        List<String> urls = new ArrayList<>();
        
        // First pass: separate exercise names and URLs
        for (String line : lines) {
            line = line.trim();
            
            String detectedMuscleGroup = detectMuscleGroup(line);
            if (detectedMuscleGroup != null) {
                // Process accumulated exercises before switching muscle groups
                exercises.addAll(matchExercisesWithUrls(exerciseNames, urls, currentMuscleGroup));
                exerciseNames.clear();
                urls.clear();
                
                currentMuscleGroup = detectedMuscleGroup;
                log.debug("Found muscle group: {}", currentMuscleGroup);
                continue;
            }
            
            if (line.isEmpty() || line.startsWith("EXERCÍCIO") || line.startsWith("VÍDEO") || 
                line.contains("Execução em Vídeo") || line.startsWith("Leandro Twin") ||
                line.startsWith("CREF:") || line.startsWith("WhatsApp:") || line.startsWith("www.")) {
                continue;
            }
            
            // Check if line contains a URL
            Matcher urlMatcher = URL_PATTERN.matcher(line);
            if (urlMatcher.find()) {
                urls.add(urlMatcher.group());
            } else if (currentMuscleGroup != null && !line.isEmpty() && line.length() >= 3) {
                // This is likely an exercise name
                String cleanName = cleanExerciseName(line);
                if (!cleanName.isEmpty() && cleanName.length() >= 3) {
                    exerciseNames.add(cleanName);
                }
            }
        }
        
        // Process remaining exercises
        exercises.addAll(matchExercisesWithUrls(exerciseNames, urls, currentMuscleGroup));
        
        return exercises;
    }
    
    private List<ParsedExerciseData> matchExercisesWithUrls(List<String> exerciseNames, 
                                                            List<String> urls, 
                                                            String muscleGroup) {
        List<ParsedExerciseData> exercises = new ArrayList<>();
        
        if (muscleGroup == null) {
            return exercises;
        }
        
        // Match exercises with URLs (may have more exercises than URLs or vice versa)
        for (int i = 0; i < exerciseNames.size(); i++) {
            String exerciseName = exerciseNames.get(i);
            String videoUrl = i < urls.size() ? urls.get(i) : null;
            
            ParsedExerciseData exercise = ParsedExerciseData.builder()
                    .name(exerciseName)
                    .videoUrl(videoUrl)
                    .muscleGroupPortuguese(muscleGroup)
                    .build();
            
            exercises.add(exercise);
            log.debug("Matched exercise: {} with URL: {}", exerciseName, videoUrl);
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

    private String cleanExerciseName(String name) {
        name = name.replaceAll("\\s+", " ").trim();
        name = name.replaceAll("^[\\d.\\-]+\\s*", "");
        name = name.replaceAll("[|\\t]+", " ");
        
        return name.trim();
    }
}
