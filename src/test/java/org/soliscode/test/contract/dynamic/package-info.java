/// Dynamic contract implementations for testing breakable collections.
///
/// This package provides specialized implementations of `DynamicContract` for various
/// Java collection types. These contracts are designed to work with "breakable"
/// implementations (e.g., `BreakableCollection`, `BreakableMap`) to systematically
/// verify contract compliance under specific, intentional anomalies or "breaks".
///
/// ## Key Components
/// - `DynamicBrokenCollectionContract`: Dynamic contract for `Collection` testing.
/// - `DynamicBrokenDequeContract`: Dynamic contract for `Deque` testing.
/// - `DynamicBrokenIterableContract`: Dynamic contract for `Iterable` testing.
/// - `DynamicBrokenMapContract`: Dynamic contract for `Map` testing.
/// - `DynamicBrokenQueueContract`: Dynamic contract for `Queue` testing.
///
/// ## Usage
/// These classes are typically used within `TestFactory` methods in contract test
/// classes (like `MapContractTest` or `CollectionContractTest`) to generate a suite
/// of tests that target specific methods and break configurations.
///
/// @author evanbergstrom
/// @since 1.0.0
package org.soliscode.test.contract.dynamic;
