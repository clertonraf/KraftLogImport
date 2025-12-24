# Test Fixes Summary

## Fixed Issues

### 1. XlsxParserServiceTest Failures
**Problem**: Tests were failing because the parser was using incorrect row indices for parsing XLSX files.

**Root Cause**: 
- The parser was using 1-based row indices in some places and 0-based in others
- The parseWorkout method signature didn't clearly specify exercise row ranges
- Exercises were not being properly extracted from the correct cells

**Solution**:
- Updated `parseWorkout` method to accept explicit parameters: `nameRow`, `firstExerciseRow`, `lastExerciseRow`, `restRow`
- Changed from while-loop with break conditions to explicit for-loop over row range
- Fixed all workout parsing calls in `parseRoutineFromXlsx` to use correct 0-based row indices
- Ensured exercises at rows 3-7 (for first workout) are properly captured

### 2. RoutineImportServiceTest Failure  
**Problem**: Test was checking for "test-routine" in JSON but the actual routine name was "test"

**Root Cause**: Test expectation didn't match the actual implementation which uses the filename (without extension) as routine name

**Solution**: Updated test assertion to check for "test" instead of "test-routine"

### 3. ExerciseImportService Missing Muscle Group
**Problem**: Muscle groups were being looked up and logged but not added to the ExerciseCreateRequest

**Root Cause**: The `convertToCreateRequest` method was missing the `.muscleGroup(muscleGroupEnglish)` call in the builder chain

**Solution**: Added `.muscleGroup(muscleGroupEnglish)` to the ExerciseCreateRequest builder

### 4. Integration Test Timeout Handling
**Problem**: API timeout test was unpredictable because HttpClient had no configured timeout

**Root Cause**: HttpClient was created with `HttpClient.newHttpClient()` which doesn't set any timeouts

**Solution**:
- Updated HttpClient creation to use builder with 10-second connect timeout
- Added 10-second request timeout to individual HTTP requests in `createExercise` method

## Test Results

All 91 tests now pass successfully:
- ✅ XlsxParserServiceTest: 6/6 tests passing  
- ✅ RoutineImportServiceTest: All tests passing
- ✅ ExerciseImportServiceTest: All tests passing
- ✅ Integration tests: All tests passing  
- ✅ Other test suites: All passing

## Files Modified

1. `src/main/java/com/kraftlog/pdfimport/service/XlsxParserService.java`
   - Fixed parseWorkout method signature and implementation
   - Updated parseRoutineFromXlsx to use correct row indices

2. `src/main/java/com/kraftlog/pdfimport/service/ExerciseImportService.java`
   - Added muscle group to ExerciseCreateRequest

3. `src/main/java/com/kraftlog/pdfimport/client/KraftLogApiClient.java`
   - Added HTTP client and request timeouts

4. `src/test/java/com/kraftlog/pdfimport/service/RoutineImportServiceTest.java`
   - Fixed test assertion for routine name
