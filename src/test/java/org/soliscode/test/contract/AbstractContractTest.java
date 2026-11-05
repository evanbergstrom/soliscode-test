package org.soliscode.test.contract;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DynamicTest;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.breakable.Break;
import org.soliscode.test.breakable.Breakable;
import org.soliscode.test.breakable.BreakableBuilder;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.soliscode.test.assertions.AssertThrowsDifferent.assertThrowsDifferent;
import static org.soliscode.test.assertions.Assertions.assertThrowsAny;

/// Base class for contract tests that utilize dynamic test generation.
///
/// This class provides utility methods for creating JUnit 5 [DynamicTest] instances that
/// verify expected failure conditions, such as when a [org.soliscode.test.breakable.Break] is applied to a
/// [org.soliscode.test.breakable.Breakable] object or when an [InterfaceMethod] is not supported.
///
/// ## Purpose
///
/// `AbstractContractTest` simplifies the systematic testing of contract interfaces against
/// [org.soliscode.test.breakable.Breakable] implementations. It allows for the creation of tests that specifically
/// assert that an operation fails with an [AssertionFailedError] when a break is active,
/// or with an [UnsupportedOperationException] when a method is marked as unsupported.
///
/// ## Usage Example
///
/// ```java
/// public class MyContractTest extends AbstractContractTest<MyBreakable> {
///
///     @TestFactory
///     public Collection<DynamicTest> dynamicTests() {
///         return List.of(
///             failsWithBreak("testOperation() fails with MY_BREAK",
///                 MyBreakable.MY_BREAK,
///                 DynamicBrokenContract::testOperation)
///         );
///     }
///
///     @Override
///     protected <X> @NonNull X createTest(Break b, InterfaceMethod m) {
///         return (X) new DynamicBrokenContract(b, m);
///     }
/// }
/// ```
///
/// ## Thread Safety
///
/// This class is thread-safe for use by the JUnit test runner.
///
/// @param <C> the type of the [org.soliscode.test.breakable.Breakable] object being tested
/// @author evanbergstrom
/// @since 1.0
public abstract class AbstractContractTest<C extends Breakable> {

    /// Default constructor
    public AbstractContractTest() {
        super();
    }

    protected <X> DynamicTest shouldPass(final @NonNull Consumer<X> test,
                                         final @NonNull String description,
                                         final @NonNull BreakableBuilder<C> builder) {
        return dynamicTest(description, () -> assertDoesNotThrow(() -> test.accept((X) createTest(builder))));
    }

    protected <X> DynamicTest shouldFail(final @NonNull Consumer<X> test,
                                         final @NonNull String description,
                                         final @NonNull BreakableBuilder<C> builder) {
        return dynamicTest(description, () -> assertThrowsAny(() -> test.accept((X) createTest(builder))));
    }


    /// Creates an instance of the test contract with the specified break and optional method status.
    ///
    /// @return an instance of the test contract
    protected abstract @NonNull DynamicContract<C> createTest(BreakableBuilder<C> builder);
}
