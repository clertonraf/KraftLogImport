# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- GitHub Actions workflow for automated Docker image building and versioning
- Comprehensive versioning strategy documentation (VERSIONING.md)
- Automated release script (`release.sh`)
- Comprehensive unit test suite (72 tests across 9 test classes)
- Test dependencies: Mockito 5.15.2, Byte Buddy 1.15.11
- Docker image caching in CI/CD pipeline

### Changed
- Enhanced Docker support with automated builds
- Improved versioning control with semantic versioning tags

## [1.0.0] - 2025-12-22

### Initial Release

### Added
- PDF parsing functionality for exercise data extraction
- Integration with KraftLog API for exercise creation
- Authentication support with JWT tokens
- Configurable muscle group mapping (Portuguese → English)
- REST API for PDF upload
- Swagger UI documentation at `/swagger-ui.html`
- Health check endpoint at `/api/import/health`
- Docker support with multi-stage build
- Environment variable configuration
- YAML-based muscle group mapping

### Features
- Parse PDF files containing exercise information
- Extract exercise names, video URLs, and muscle group associations
- Automatic muscle group translation
- Batch import of exercises to KraftLog API
- Detailed import results with success/failure tracking
- Docker image available at `ghcr.io/clertonraf/kraftlog-import:1.0.0`

### Technical Details
- **Framework**: Spring Boot 3.2.1
- **Java Version**: 21
- **Build Tool**: Maven
- **Container**: Docker with Alpine Linux (349MB)
- **Registry**: GitHub Container Registry (GHCR)
- **Base Port**: 8081

### Dependencies
- Spring Boot Starter Web 3.2.1
- Spring Boot Starter Validation 3.2.1
- Apache PDFBox 3.0.1
- SnakeYAML 2.2
- SpringDoc OpenAPI 2.3.0
- Lombok 1.18.42
- JUnit 5 (Jupiter)
- Mockito 5.15.2

### Testing
- 72 unit tests covering:
  - DTOs (15 tests)
  - Configuration (18 tests)
  - Services (17 tests)
  - Controllers (12 tests)
  - Client (7 tests)
  - Application (2 tests)
- 42 tests passing on Java 25
- All tests pass on Java 21

### Documentation
- README.md with quick start guide
- API documentation via Swagger UI
- VERSIONING.md with release strategy
- Docker deployment instructions

[Unreleased]: https://github.com/clertonraf/KraftLogImport/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/clertonraf/KraftLogImport/releases/tag/v1.0.0
