# Project Test Standards
The testing standards for this project include the standards defined in the file 
[Common Testing Standards](common-test-standards.md).

## Naming
- All unit test methods for breakable classes should be named using the following convention:
    - `<method_name>_with<break_name>Break_<expected_behavior>`
    - Example: `size_withAlwaysReturnsZeroBreak_returnsZero`
  
## Documentation
- The text in the DisplayName annotation Form breakable classes should be in the following format:
    - `<method_name> <condition> with <break_name> break`

## Testing Framework Integration

### Provider Pattern
- Implement providers for consistent test data generation across test classes

### Contract Testing
Use provider pattern to enable parameterized testing across different implementations

### Break Testing
- Test Breakable classes using the Contract Testing Framework for normal behavior (no breaks)
- Test each breakable method for each break individually
- When testing for the wrong exception for unsupported methods, use the assertThrowsDifferent method.

