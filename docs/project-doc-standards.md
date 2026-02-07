# Documentation Standard

## Break Documentation Standards
When documenting constants representing a `Break` (e.g., in `BreakableCollection`), include:
- **Purpose**: What contract violation is being simulated.
- **Effect**: How the behavior of the method changes when the break is active.
- **Use Case**: When a tester should use this break.
- **Affected Methods**: Explicitly list which methods are changed by this break.

Example:
```java
/// #### ADD_DOES_NOT_ADD_ELEMENT
/// **Purpose**: Forces `add_singleElement_returnsTrueAndUpdatesSize()` method to accept elements but not actually add_singleElement_returnsTrueAndUpdatesSize them to the collection
/// **Effect**: Method returns normally but collection remains unchanged
/// **Use Case**: Testing code that assumes successful add_singleElement_returnsTrueAndUpdatesSize operations modify the collection
/// **Affected Methods**: `add_singleElement_returnsTrueAndUpdatesSize(E)`
public static final Break ADD_DOES_NOT_ADD_ELEMENT;
```

## Contract Interface Documentation Standards
For `Contract` interfaces (e.g., `SizeContract`), documentation must specify:
- The target JDK method(s) being tested.
- Requirements for the `provider()` and other support methods.
- Standard generic parameters (`<E>`, `<C>`, etc.).
- Link to the target method using `@see`.