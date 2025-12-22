# KraftLog System Integration Guide

This guide shows how to run both KraftLog API and KraftLog PDF Import services together.

## System Architecture

```
                    ┌─────────────────────────┐
                    │   Client/Browser        │
                    │   (Upload PDF)          │
                    └───────────┬─────────────┘
                                │
                                │ HTTP
                                │
                    ┌───────────▼──────────────┐
                    │  KraftLog PDF Import     │
                    │  Port: 8081              │
                    │  ┌────────────────────┐  │
                    │  │ - PDF Parser       │  │
                    │  │ - API Client       │  │
                    │  │ - Auth Handler     │  │
                    │  └────────────────────┘  │
                    └───────────┬──────────────┘
                                │
                                │ REST API (JWT)
                                │
                    ┌───────────▼──────────────┐
                    │  KraftLog API            │
                    │  Port: 8080              │
                    │  ┌────────────────────┐  │
                    │  │ - Authentication   │  │
                    │  │ - Exercise CRUD    │  │
                    │  │ - Workout Mgmt     │  │
                    │  │ - User Mgmt        │  │
                    │  └────────────────────┘  │
                    └───────────┬──────────────┘
                                │
                                │ JDBC
                                │
                    ┌───────────▼──────────────┐
                    │  PostgreSQL              │
                    │  Port: 5432              │
                    │  Database: kraftlog      │
                    └──────────────────────────┘
```

## Running Both Services

### Option 1: Local Development (Separate Terminals)

#### Terminal 1: Start KraftLog API
```bash
cd /Users/clerton/workspace/KraftLogApi
mvn spring-boot:run
```
*API will be available at http://localhost:8080*

#### Terminal 2: Start KraftLog PDF Import
```bash
cd /Users/clerton/workspace/KraftLogPDFImport

# Set environment variables
export KRAFTLOG_API_URL=http://localhost:8080
export KRAFTLOG_API_USERNAME=admin
export KRAFTLOG_API_PASSWORD=admin

# Run the service
mvn spring-boot:run
```
*Import service will be available at http://localhost:8081*

### Option 2: Docker Compose (Recommended)

The KraftLogPDFImport repository includes a `docker-compose.yml` that runs the entire stack:

```bash
cd /Users/clerton/workspace/KraftLogPDFImport
docker-compose up -d
```

This starts:
- PostgreSQL (port 5432)
- KraftLog API (port 8080)
- KraftLog PDF Import (port 8081)

### Option 3: Standalone Docker Containers

#### Start PostgreSQL
```bash
docker run -d \
  --name kraftlog-postgres \
  -e POSTGRES_DB=kraftlog \
  -e POSTGRES_USER=kraftlog \
  -e POSTGRES_PASSWORD=kraftlog \
  -p 5432:5432 \
  postgres:16-alpine
```

#### Start KraftLog API
```bash
cd /Users/clerton/workspace/KraftLogApi
docker build -t kraftlog-api .
docker run -d \
  --name kraftlog-api \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/kraftlog \
  kraftlog-api
```

#### Start KraftLog PDF Import
```bash
cd /Users/clerton/workspace/KraftLogPDFImport
docker build -t kraftlog-pdf-import .
docker run -d \
  --name kraftlog-pdf-import \
  -p 8081:8081 \
  -e KRAFTLOG_API_URL=http://host.docker.internal:8080 \
  -e KRAFTLOG_API_USERNAME=admin \
  -e KRAFTLOG_API_PASSWORD=admin \
  kraftlog-pdf-import
```

## Verification Steps

### 1. Check All Services are Running

```bash
# Check KraftLog API
curl http://localhost:8080/actuator/health
# or
curl http://localhost:8080/api/exercises

# Check PDF Import Service
curl http://localhost:8081/api/import/health
```

### 2. Test Authentication

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

Expected response: `{"token":"...", "username":"admin", "role":"ADMIN"}`

### 3. Test PDF Import

```bash
curl -X POST http://localhost:8081/api/import/pdf \
  -F "file=@/path/to/exercises.pdf"
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

### 4. Verify Exercises Were Created

```bash
# Get auth token first
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' | jq -r '.token')

# List all exercises
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/exercises
```

## API Endpoints Summary

### KraftLog API (Port 8080)

#### Authentication
- `POST /api/auth/login` - Login and get JWT token
- `POST /api/auth/register` - Register new user

#### Exercises
- `GET /api/exercises` - List all exercises
- `POST /api/exercises` - Create exercise (Admin only)
- `GET /api/exercises/{id}` - Get exercise by ID
- `PUT /api/exercises/{id}` - Update exercise (Admin only)
- `DELETE /api/exercises/{id}` - Delete exercise (Admin only)

#### Other
- `GET /api/workouts` - List workouts
- `GET /api/muscles` - List muscles
- `GET /swagger-ui.html` - API Documentation

### KraftLog PDF Import (Port 8081)

#### Import
- `POST /api/import/pdf` - Import exercises from PDF
- `GET /api/import/health` - Health check
- `GET /swagger-ui.html` - API Documentation

## Communication Flow

### PDF Import Process

1. **Client → PDF Import**: Upload PDF file
   ```
   POST /api/import/pdf
   Content-Type: multipart/form-data
   ```

2. **PDF Import**: Parse PDF to extract exercises
   - Extract text using PDFBox
   - Identify muscle groups
   - Parse exercise names and URLs

3. **PDF Import → API**: Authenticate
   ```
   POST /api/auth/login
   Body: {"username":"admin","password":"admin"}
   Response: {"token":"jwt_token_here"}
   ```

4. **PDF Import → API**: Create each exercise
   ```
   POST /api/exercises
   Headers: Authorization: Bearer jwt_token_here
   Body: {
     "name": "Exercise Name",
     "videoUrl": "https://youtu.be/...",
     ...
   }
   ```

5. **API → Database**: Store exercises in PostgreSQL

6. **PDF Import → Client**: Return import results
   ```json
   {
     "status": "success",
     "totalProcessed": 50,
     "successful": 48,
     "failed": 2
   }
   ```

## Configuration Files

### KraftLog API
`/Users/clerton/workspace/KraftLogApi/src/main/resources/application.yml`
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/kraftlog
    username: kraftlog
    password: kraftlog
```

### KraftLog PDF Import
`/Users/clerton/workspace/KraftLogPDFImport/src/main/resources/application.yml`
```yaml
server:
  port: 8081

kraftlog:
  api:
    base-url: http://localhost:8080
    auth:
      username: admin
      password: admin
```

## Environment Variables

### For KraftLog API
- `SPRING_DATASOURCE_URL` - Database connection URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password
- `JWT_SECRET` - Secret for JWT token signing

### For KraftLog PDF Import
- `KRAFTLOG_API_URL` - KraftLog API base URL
- `KRAFTLOG_API_USERNAME` - Admin username
- `KRAFTLOG_API_PASSWORD` - Admin password
- `EXERCISE_MUSCLE_GROUPS_CONFIG_PATH` - Muscle groups config file

## Troubleshooting

### PDF Import Can't Connect to API

**Problem**: `Connection refused` or `Cannot connect to KraftLog API`

**Solutions**:
1. Verify API is running: `curl http://localhost:8080/api/exercises`
2. Check `KRAFTLOG_API_URL` environment variable
3. If using Docker, use `host.docker.internal` instead of `localhost`

### Authentication Failed

**Problem**: `Authentication failed with status: 401`

**Solutions**:
1. Verify admin credentials are correct
2. Check admin user exists in database
3. Try logging in manually: `curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin"}'`

### No Exercises Created

**Problem**: PDF import succeeds but no exercises appear

**Solutions**:
1. Check PDF Import logs for errors
2. Verify exercises are being sent to API
3. Check API logs for errors
4. Verify database connectivity
5. Check if exercises already exist (unique name constraint)

### Port Already in Use

**Problem**: `Port 8080 already in use` or `Port 8081 already in use`

**Solutions**:
1. Stop the conflicting service
2. Change port in `application.yml`
3. Use Docker with port mapping: `-p 8082:8080`

## Monitoring

### Logs

#### KraftLog API Logs
```bash
# If running with Maven
tail -f /Users/clerton/workspace/KraftLogApi/logs/spring.log

# If running with Docker
docker logs -f kraftlog-api
```

#### PDF Import Logs
```bash
# If running with Maven
tail -f /Users/clerton/workspace/KraftLogPDFImport/logs/spring.log

# If running with Docker
docker logs -f kraftlog-pdf-import
```

### Health Checks

```bash
# Check all services
curl http://localhost:8080/actuator/health
curl http://localhost:8081/api/import/health
curl -u kraftlog:kraftlog http://localhost:5432  # PostgreSQL
```

## Performance Considerations

### For Large PDF Files
- PDF Import parses files in memory
- Consider file size limits in `application.yml`:
  ```yaml
  spring:
    servlet:
      multipart:
        max-file-size: 10MB
        max-request-size: 10MB
  ```

### For Many Exercises
- Import processes exercises sequentially
- Each exercise requires an API call
- Large imports may take several minutes
- Consider adding progress reporting for UI

## Security Considerations

1. **Use Strong Passwords**: Change default admin password
2. **HTTPS**: Use HTTPS in production
3. **JWT Secret**: Set a strong JWT secret
4. **Network Security**: Restrict database access
5. **Environment Variables**: Don't commit credentials to git

## Production Deployment

### Recommended Setup

1. **Reverse Proxy** (Nginx/Apache)
   - Handle SSL/TLS termination
   - Route `/api` to KraftLog API
   - Route `/import` to PDF Import

2. **Environment Variables**
   - Use secret management (e.g., AWS Secrets Manager)
   - Rotate credentials regularly

3. **Monitoring**
   - Add health check endpoints
   - Monitor logs
   - Set up alerts

4. **Scaling**
   - Run multiple instances behind load balancer
   - PDF Import can scale independently
   - API can scale independently

## Backup and Recovery

### Database Backup
```bash
pg_dump -U kraftlog kraftlog > backup.sql
```

### Restore Database
```bash
psql -U kraftlog kraftlog < backup.sql
```

## Summary

✅ **KraftLog API** - Core API on port 8080
✅ **KraftLog PDF Import** - Import service on port 8081
✅ **PostgreSQL** - Database on port 5432
✅ **Communication** - PDF Import → API via REST/JWT
✅ **Documentation** - Swagger UI on both services

Both services work together seamlessly to provide a complete exercise management and import solution!

---

**Last Updated**: 2025-12-22
**System Version**: API v1.1.0 + PDF Import v1.0.0
