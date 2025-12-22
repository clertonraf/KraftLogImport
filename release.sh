#!/bin/bash

# Release script for KraftLog PDF Import
# Usage: ./release.sh [major|minor|patch]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if git is clean
if [[ -n $(git status -s) ]]; then
    echo -e "${RED}Error: Git working directory is not clean. Please commit or stash changes.${NC}"
    exit 1
fi

# Get current version from pom.xml
CURRENT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
echo -e "${GREEN}Current version: ${CURRENT_VERSION}${NC}"

# Parse version parts
IFS='.' read -r MAJOR MINOR PATCH <<< "${CURRENT_VERSION%-SNAPSHOT}"

# Determine new version
case "$1" in
    major)
        NEW_VERSION="$((MAJOR + 1)).0.0"
        ;;
    minor)
        NEW_VERSION="${MAJOR}.$((MINOR + 1)).0"
        ;;
    patch)
        NEW_VERSION="${MAJOR}.${MINOR}.$((PATCH + 1))"
        ;;
    *)
        echo -e "${RED}Error: Invalid argument. Use 'major', 'minor', or 'patch'${NC}"
        echo "Usage: $0 [major|minor|patch]"
        exit 1
        ;;
esac

echo -e "${YELLOW}New version will be: ${NEW_VERSION}${NC}"
read -p "Continue? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Aborted."
    exit 1
fi

# Update version in pom.xml
echo -e "${GREEN}Updating pom.xml...${NC}"
mvn versions:set -DnewVersion=${NEW_VERSION} -DgenerateBackupPoms=false

# Commit version change
echo -e "${GREEN}Committing version change...${NC}"
git add pom.xml
git commit -m "chore: bump version to ${NEW_VERSION}"

# Create git tag
echo -e "${GREEN}Creating git tag v${NEW_VERSION}...${NC}"
git tag -a "v${NEW_VERSION}" -m "Release version ${NEW_VERSION}"

# Push changes and tag
echo -e "${GREEN}Pushing to remote...${NC}"
git push origin master
git push origin "v${NEW_VERSION}"

echo -e "${GREEN}✅ Release ${NEW_VERSION} created successfully!${NC}"
echo ""
echo "GitHub Actions will now:"
echo "  1. Run tests"
echo "  2. Build the application"
echo "  3. Create Docker images with tags:"
echo "     - ghcr.io/clertonraf/kraftlog-pdf-import:${NEW_VERSION}"
echo "     - ghcr.io/clertonraf/kraftlog-pdf-import:${MAJOR}.${MINOR}"
echo "     - ghcr.io/clertonraf/kraftlog-pdf-import:${MAJOR}"
echo "     - ghcr.io/clertonraf/kraftlog-pdf-import:latest"
echo ""
echo "Check progress at: https://github.com/clertonraf/KraftLogPDFImport/actions"
