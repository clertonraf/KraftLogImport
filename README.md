# KraftLog Import

A standalone Spring Boot application for importing exercises and routines into the KraftLog API.

## Overview

This service parses PDF files containing exercise data and XLSX files containing routine data (in Portuguese format) and automatically imports them into KraftLog API. It handles authentication, parsing, and data transformation.

## Features

- **PDF Parsing**: Extracts exercise names, video URLs, and muscle group information from structured PDFs
- **XLSX Parsing**: Extracts complete routine structures with workouts, exercises, sets, reps, and rest intervals
- **KraftLog API Integration**: Authenticates with KraftLog API and creates exercises and routines
- **Configurable**: Easy configuration for KraftLog API URL and credentials
- **Muscle Group Mapping**: Maps Portuguese muscle group names to English equivalents
- **RESTful API**: Provides endpoints for PDF and XLSX upload and processing
- **Swagger UI**: Interactive API documentation at `/swagger-ui.html`

## Requirements

- Java 21
- Maven 3.6+
- Running instance of KraftLog API
- Admin credentials for KraftLog API

## Configuration

Configure the application using environment variables or `application.yml`:

### Environment Variables

- `KRAFTLOG_API_URL`: Base URL of the KraftLog API (default: `http://localhost:8080`)
- `KRAFTLOG_API_USERNAME`: Admin username for KraftLog API (default: `admin`)
- `KRAFTLOG_API_PASSWORD`: Admin password for KraftLog API (default: `admin`)
- `EXERCISE_MUSCLE_GROUPS_CONFIG_PATH`: Path to muscle group mapping YAML file (default: `exercise-muscle-groups.yml`)

### Example Configuration

```bash
export KRAFTLOG_API_URL=http://localhost:8080
export KRAFTLOG_API_USERNAME=admin
export KRAFTLOG_API_PASSWORD=your_password
export EXERCISE_MUSCLE_GROUPS_CONFIG_PATH=/path/to/exercise-muscle-groups.yml
```

## Muscle Group Mapping

Create a YAML file to map Portuguese muscle group names to English equivalents:

```yaml
# exercise-muscle-groups.yml
PEITO: CHEST
COSTAS: BACK
OMBROS: SHOULDERS
BÍCEPS: BICEPS
TRÍCEPS: TRICEPS
PERNAS: LEGS
GLÚTEOS: GLUTES
ABDÔMEN: ABS
ANTEBRAÇOS: FOREARMS
PANTURRILHAS: CALVES
```

## Building

```bash
mvn clean package
```

## Running

### Using Maven

```bash
mvn spring-boot:run
```

### Using JAR

```bash
java -jar target/kraftlog-import-1.0.0.jar
```

The application will start on port 8081 by default.

## Usage

### API Endpoints

#### Import PDF
- **POST** `/api/import/pdf`
- **Content-Type**: `multipart/form-data`
- **Parameter**: `file` (PDF file)

Example using curl:

```bash
curl -X POST http://localhost:8081/api/import/pdf \
  -F "file=@exercises.pdf"
```

#### Import XLSX Routine
- **POST** `/api/import/routine`
- **Content-Type**: `multipart/form-data`
- **Parameters**: 
  - `file` (XLSX file)
  - `username` (KraftLog user to create routine for)

Example using curl:

```bash
curl -X POST http://localhost:8081/api/import/routine \
  -F "file=@routine.xlsx" \
  -F "username=myuser"
```

Response:

```json
{
  "status": "success",
  "message": "Import completed",
  "totalProcessed": 50,
  "successful": 48,
  "failed": 2,
  "failures": [
    {
      "exerciseName": "Exercise Name",
      "reason": "Error message"
    }
  ]
}
```

#### Health Check
- **GET** `/api/import/health`

### Swagger UI

Access the interactive API documentation at: `http://localhost:8081/swagger-ui.html`

## PDF Format

The PDF should be structured as follows:

1. **Muscle Group Headers**: Text lines containing muscle group names (e.g., "PEITO", "COSTAS")
2. **Exercise Tables**: Following each header, exercises should be listed with:
   - Exercise name
   - YouTube video URL (optional)

Example:

```
PEITO

Supino Reto com Barra https://youtu.be/example1
Supino Inclinado com Halteres https://youtu.be/example2
Crucifixo

COSTAS

Remada Curvada https://youtu.be/example3
Puxada Frontal
```

## How It Works

1. **PDF Upload**: User uploads a PDF via the REST API
2. **PDF Parsing**: The service extracts exercise data from the PDF
3. **Authentication**: Authenticates with KraftLog API using configured credentials
4. **Exercise Creation**: For each parsed exercise:
   - Converts to `ExerciseCreateRequest` format
   - Calls KraftLog API's `/api/exercises` endpoint
   - Handles authentication token refresh if needed
5. **Results**: Returns summary of successful and failed imports

## Architecture

- **Controller Layer**: REST API endpoints for PDF upload
- **Service Layer**: 
  - `PdfParserService`: PDF parsing logic
  - `ExerciseImportService`: Import orchestration
- **Client Layer**: `KraftLogApiClient` - HTTP client for KraftLog API
- **Config Layer**: Configuration properties and muscle group mapping

## Logging

Logs are configured with DEBUG level for the application package. Check logs for:
- Authentication status
- PDF parsing progress
- Exercise creation results
- Error messages

## Error Handling

The service handles various error scenarios:
- Invalid PDF format
- Empty PDF files
- Authentication failures
- Network errors
- API errors

All errors are logged and included in the response.

## Development

### Project Structure

```
src/main/java/com/kraftlog/pdfimport/
├── KraftLogPdfImportApplication.java
├── client/
│   └── KraftLogApiClient.java
├── config/
│   ├── KraftLogApiProperties.java
│   └── MuscleGroupMappingConfig.java
├── controller/
│   └── ImportController.java
├── dto/
│   ├── ExerciseCreateRequest.java
│   └── ParsedExerciseData.java
└── service/
    ├── ExerciseImportService.java
    └── PdfParserService.java
```

## License

Same as KraftLog API

## Support

For issues or questions, please refer to the main KraftLog API repository.
