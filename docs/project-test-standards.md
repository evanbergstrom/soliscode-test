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

#### Collection Interface Test Contracts
- Implement contracts for each method of the Collection interface
- Use the Contract Testing Framework to test each contract for normal behavior (no breaks)
- Test each contract for each break individually
- Use the a breakable collection to test contracts for the corresponding collection interface.
- When testing for the wrong exception for unsupported methods, use the assertThrowsDifferent method.

For each collection method, write tests for the following:
- When the collection is empty,
- When the collection contains elements,
- When a 'null' element is passed to the method.
- When a 'null' element is passed to the method of a collection that does not accept nulls
- When duplicate elements are added to the collection
- When duplicate elements are added to a collection that does not permit duplicates
- When an elements of an incompatible type is is passed to the method of a collection that does not permit duplicates
- When a method is called that is not supported by the collection

For methods that accept a collection of elements, write tests for the following:
- When an empty collection is passed to the method.
- When a collection containing elements is passed to the method.
- When a collection containing at least one null is passed to the method.
- When a collection containing at least one null is passed to the method of a collection that does not accept nulls.
- When a collection containing duplicate elements is passed to the method.
- When a collection containing duplicate elements is passed to the method of a collection that does not permit duplicates.
- When a collection containing elements of an incompatible type is passed to the method.
- When a collection containing elements of an incompatible type is passed to the method of a collection that does not permit duplicates.


### Break Testing
- Test Breakable classes using the Contract Testing Framework for normal behavior (no breaks)
- Test each breakable method for each break individually
- When testing for the wrong exception for unsupported methods, use the assertThrowsDifferent method.

