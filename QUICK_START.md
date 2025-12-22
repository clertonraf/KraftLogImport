# Quick Start Guide - KraftLog PDF Import

This guide will help you quickly set up and start using the KraftLog PDF Import service.

## Prerequisites

- Java 21 installed
- Maven 3.6+ installed
- KraftLog API running (default: http://localhost:8080)
- Admin credentials for KraftLog API

## Step 1: Clone and Build

```bash
cd /path/to/workspace
cd KraftLogPDFImport
mvn clean package
```

## Step 2: Configure

Create or edit the `.env` file (or set environment variables):

```bash
export KRAFTLOG_API_URL=http://localhost:8080
export KRAFTLOG_API_USERNAME=admin
export KRAFTLOG_API_PASSWORD=your_password
export EXERCISE_MUSCLE_GROUPS_CONFIG_PATH=exercise-muscle-groups.yml
```

## Step 3: Verify Configuration File

Ensure `exercise-muscle-groups.yml` exists in the project root:

```yaml
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

## Step 4: Run the Application

```bash
mvn spring-boot:run
```

Or using the JAR:

```bash
java -jar target/kraftlog-pdf-import-1.0.0.jar
```

The service will start on **port 8081**.

## Step 5: Test the Service

### Health Check

```bash
curl http://localhost:8081/api/import/health
```

Expected response:
```json
{
  "status": "UP",
  "service": "KraftLog PDF Import"
}
```

### Import a PDF

```bash
curl -X POST http://localhost:8081/api/import/pdf \
  -F "file=@/path/to/your/exercises.pdf"
```

Expected response:
```json
{
  "status": "success",
  "message": "Import completed",
  "totalProcessed": 50,
  "successful": 48,
  "failed": 2,
  "failures": [...]
}
```

## Step 6: Access Swagger UI

Open your browser and navigate to:

```
http://localhost:8081/swagger-ui.html
```

You can test the API endpoints directly from the Swagger interface.

## Using Docker (Optional)

### Build and Run with Docker Compose

```bash
docker-compose up -d
```

This will start:
- PostgreSQL database (port 5432)
- KraftLog API (port 8080)
- KraftLog PDF Import (port 8081)

### Build Only PDF Import Service

```bash
docker build -t kraftlog-pdf-import .
docker run -p 8081:8081 \
  -e KRAFTLOG_API_URL=http://host.docker.internal:8080 \
  -e KRAFTLOG_API_USERNAME=admin \
  -e KRAFTLOG_API_PASSWORD=admin \
  kraftlog-pdf-import
```

## PDF Format Requirements

Your PDF should be structured like this:

```
PEITO

Supino Reto com Barra https://youtu.be/xxxxx
Supino Inclinado https://youtu.be/yyyyy
Crucifixo

COSTAS

Remada Curvada https://youtu.be/zzzzz
Puxada Frontal
```

Key points:
- Muscle group names must match those in `exercise-muscle-groups.yml`
- Exercise names followed by optional YouTube URLs
- One exercise per line

## Troubleshooting

### Cannot Connect to KraftLog API

- Verify KraftLog API is running: `curl http://localhost:8080/api/auth/health`
- Check the `KRAFTLOG_API_URL` environment variable
- Ensure network connectivity

### Authentication Failed

- Verify admin credentials are correct
- Check that the admin user exists in KraftLog API
- Review logs: `tail -f logs/application.log`

### No Exercises Found in PDF

- Verify PDF format matches expected structure
- Check muscle group names match `exercise-muscle-groups.yml`
- Enable DEBUG logging to see parsing details

### Port Already in Use

Change the port in `application.yml`:

```yaml
server:
  port: 8082
```

## Next Steps

- Customize `exercise-muscle-groups.yml` for your language/needs
- Integrate with your CI/CD pipeline
- Set up monitoring and logging
- Configure SSL/TLS for production

## Support

For issues or questions:
1. Check the main README.md
2. Review application logs
3. Consult the KraftLog API documentation

## Useful Commands

```bash
# Build without tests
mvn clean package -DskipTests

# Run with custom port
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8082

# View logs in real-time
tail -f logs/application.log

# Stop Docker services
docker-compose down

# Check running containers
docker ps
```

Happy importing! 🚀
