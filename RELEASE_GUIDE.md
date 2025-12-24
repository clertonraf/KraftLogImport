# Quick Release Guide

## Release a New Version

### Patch Release (Bug fixes)
```bash
./release.sh patch
# 1.0.0 → 1.0.1
```

### Minor Release (New features, backward compatible)
```bash
./release.sh minor
# 1.0.0 → 1.1.0
```

### Major Release (Breaking changes)
```bash
./release.sh major
# 1.0.0 → 2.0.0
```

## What Happens Automatically

1. ✅ Updates `pom.xml` version
2. ✅ Creates git commit
3. ✅ Creates git tag (`v1.x.x`)
4. ✅ Pushes to GitHub
5. ✅ Triggers GitHub Actions
6. ✅ Runs all tests
7. ✅ Builds Docker image
8. ✅ Tags with multiple versions
9. ✅ Pushes to GitHub Container Registry

## Available Docker Tags

After release `v1.2.3`, you can use:
- `ghcr.io/clertonraf/kraftlog-import:1.2.3` (specific)
- `ghcr.io/clertonraf/kraftlog-import:1.2` (minor)
- `ghcr.io/clertonraf/kraftlog-import:1` (major)
- `ghcr.io/clertonraf/kraftlog-import:latest` (latest stable)

## Check Build Status

- **Actions**: https://github.com/clertonraf/KraftLogImport/actions
- **Images**: https://github.com/clertonraf/KraftLogImport/pkgs/container/kraftlog-import

## Rollback

```bash
# Revert the commit
git revert HEAD
git push origin master

# Or checkout previous version
docker pull ghcr.io/clertonraf/kraftlog-import:1.0.0
```

## Notes

- Always ensure tests pass before releasing
- Keep `CHANGELOG.md` updated
- Use semantic versioning (MAJOR.MINOR.PATCH)
- Pin specific versions in production
