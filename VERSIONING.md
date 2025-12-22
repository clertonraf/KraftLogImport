# Versioning Strategy

This project uses [Semantic Versioning](https://semver.org/) (SemVer) for version control.

## Version Format

```
MAJOR.MINOR.PATCH
```

- **MAJOR**: Incremented for incompatible API changes
- **MINOR**: Incremented for new functionality in a backward-compatible manner
- **PATCH**: Incremented for backward-compatible bug fixes

## Current Version

The current version is defined in `pom.xml`:
```xml
<version>1.0.0</version>
```

## Docker Image Tagging Strategy

Docker images are automatically tagged based on the git context:

### Automatic Tags

1. **Branch-based tags** (on push to branches):
   - `master` → `ghcr.io/clertonraf/kraftlog-pdf-import:master`
   - `main` → `ghcr.io/clertonraf/kraftlog-pdf-import:main`
   - `latest` → automatically tagged for default branch

2. **Semantic version tags** (on git tags):
   - `v1.2.3` → `ghcr.io/clertonraf/kraftlog-pdf-import:1.2.3`
   - `v1.2.3` → `ghcr.io/clertonraf/kraftlog-pdf-import:1.2`
   - `v1.2.3` → `ghcr.io/clertonraf/kraftlog-pdf-import:1`

3. **Commit SHA tags** (for traceability):
   - `ghcr.io/clertonraf/kraftlog-pdf-import:master-abc1234`

4. **Pull Request tags**:
   - `ghcr.io/clertonraf/kraftlog-pdf-import:pr-123`

## How to Release a New Version

### Step 1: Update Version in pom.xml

```bash
# Update version in pom.xml
mvn versions:set -DnewVersion=1.1.0
mvn versions:commit
```

### Step 2: Commit the Version Change

```bash
git add pom.xml
git commit -m "chore: bump version to 1.1.0"
```

### Step 3: Create a Git Tag

```bash
# Create an annotated tag
git tag -a v1.1.0 -m "Release version 1.1.0"

# Push the commit and tag
git push origin master
git push origin v1.1.0
```

### Step 4: Verify Docker Images

The GitHub Action will automatically:
- Run tests
- Build the application
- Create and push Docker images with appropriate tags

Check the Actions tab: https://github.com/clertonraf/KraftLogPDFImport/actions

## Version History

| Version | Date       | Description |
|---------|------------|-------------|
| 1.0.0   | 2025-12-22 | Initial release with PDF import functionality |

## Docker Image Tags in Registry

You can view all available tags at:
https://github.com/clertonraf/KraftLogPDFImport/pkgs/container/kraftlog-pdf-import

## Example Usage by Version

### Use latest stable version:
```bash
docker pull ghcr.io/clertonraf/kraftlog-pdf-import:latest
```

### Use specific version:
```bash
docker pull ghcr.io/clertonraf/kraftlog-pdf-import:1.0.0
```

### Use major version (always gets latest minor/patch):
```bash
docker pull ghcr.io/clertonraf/kraftlog-pdf-import:1
```

### Use specific commit:
```bash
docker pull ghcr.io/clertonraf/kraftlog-pdf-import:master-abc1234
```

## Continuous Integration

The GitHub Actions workflow automatically:
- ✅ Runs on every push to `master` or `main`
- ✅ Runs on every pull request
- ✅ Runs on every tag push matching `v*.*.*`
- ✅ Runs Maven tests before building
- ✅ Creates multi-architecture Docker images
- ✅ Pushes to GitHub Container Registry
- ✅ Uses Docker layer caching for faster builds

## Rollback Strategy

To rollback to a previous version:

```bash
# Pull the previous version
docker pull ghcr.io/clertonraf/kraftlog-pdf-import:1.0.0

# Or use git to revert
git revert <commit-hash>
git push origin master
```

## Best Practices

1. **Always create release tags** for production deployments
2. **Use semantic versioning** for clarity
3. **Document breaking changes** in CHANGELOG.md
4. **Test before tagging** a new version
5. **Use `latest` tag** only for development/testing
6. **Pin specific versions** in production environments

## Automation

The project uses GitHub Actions for automated:
- Testing
- Building
- Docker image creation
- Versioning
- Registry publishing

See `.github/workflows/docker-build.yml` for configuration details.
