package org.soliscode.test.contract;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DynamicTest;
import org.soliscode.test.breakable.Breakable;
import org.soliscode.test.breakable.BreakableBuilder;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.soliscode.test.assertions.Assertions.assertThrowsAny;

/// Base class for contract tests that utilize dynamic test generation.
///
/// Each test runs one of a contract's test methods against objects built by a builder, and expects it to pass (the
/// objects are correct) or to fail (the builder gave them a break). Collection contracts use
/// [AbstractCollectionContractTest], which adds the contract's configuration.
///
/// ## Usage Example
///
/// ```java
/// public class MapContractTest extends AbstractContractTest<BreakableMap<Integer, String>,
///         BreakableMapBuilder<BreakableMap<Integer, String>, Integer, String>> {
///
///     @TestFactory
///     public Collection<DynamicTest> dynamicTests() {
///         return List.of(
///             shouldFail(DynamicMapTest::size_whenEmpty_returnsZero, "size() fails with SIZE_IS_OFF_BY_ONE",
///                 new BreakableMap.Builder<Integer, String>().addBreak(BreakableMap.SIZE_IS_OFF_BY_ONE)));
///     }
///
///     @Override
///     protected DynamicMapTest createTest(BreakableMapBuilder<BreakableMap<Integer, String>, Integer, String> b) {
///         return new DynamicMapTest(b);
///     }
/// }
/// ```
///
/// @param <C> the type of the [org.soliscode.test.breakable.Breakable] object being tested
/// @param <B> the type of builder that builds it
/// @author evanbergstrom
/// @since 1.0
public abstract class AbstractContractTest<C extends Breakable, B extends BreakableBuilder<C>> {

    /// Default constructor.
    public AbstractContractTest() {
        super();
    }

    /// Creates a test that runs a contract test method and expects it to pass.
    ///
    /// @param <X> the type of contract
    /// @param test the contract test method
    /// @param description the test's display name
    /// @param builder the builder of the objects under test
    /// @return a dynamic test
    @SuppressWarnings("unchecked")
    protected <X> DynamicTest shouldPass(final @NonNull Consumer<X> test,
                                         final @NonNull String description,
                                         final @NonNull B builder) {
        return dynamicTest(description, () -> assertDoesNotThrow(() -> test.accept((X) createTest(builder))));
    }

    /// Creates a test that runs a contract test method and expects it to fail.
    ///
    /// @param <X> the type of contract
    /// @param test the contract test method
    /// @param description the test's display name
    /// @param builder the builder of the objects under test
    /// @return a dynamic test
    @SuppressWarnings("unchecked")
    protected <X> DynamicTest shouldFail(final @NonNull Consumer<X> test,
                                         final @NonNull String description,
                                         final @NonNull B builder) {
        return dynamicTest(description, () -> assertThrowsAny(() -> test.accept((X) createTest(builder))));
    }

    /// Creates the contract to test, for objects built by a builder.
    ///
    /// @param builder the builder of the objects under test
    /// @return the contract
    protected abstract @NonNull DynamicContract<C> createTest(@NonNull B builder);
}
