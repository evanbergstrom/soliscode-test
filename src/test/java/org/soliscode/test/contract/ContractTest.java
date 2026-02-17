package org.soliscode.test.contract;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DynamicTest;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.breakable.Break;

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
/// `ContractTest` simplifies the systematic testing of contract interfaces against
/// [org.soliscode.test.breakable.Breakable] implementations. It allows for the creation of tests that specifically
/// assert that an operation fails with an [AssertionFailedError] when a break is active,
/// or with an [UnsupportedOperationException] when a method is marked as unsupported.
///
/// ## Usage Example
///
/// ```java
/// public class MyContractTest extends ContractTest<MyBreakable> {
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
public abstract class ContractTest<C> {

    /// Creates a dynamic test that is expected to fail with an [AssertionFailedError]
    /// due to a specific break being applied.
    ///
    /// @param <X>         the type of the test contract
    /// @param aBreak      the break to apply to the test object
    /// @param test        the test method to call, which is expected to fail
    /// @param description a description for the dynamic test
    /// @return a dynamic test instance
    @SuppressWarnings("unchecked")
    protected <X> DynamicTest failsWithBreak(final @NonNull Break aBreak, final @NonNull Consumer<X> test,
                                             final @NonNull String description) {
        return dynamicTest(description, () -> assertThrowsAny(() -> test.accept((X) createTest(aBreak))));
    }

    /// Creates a dynamic test that is expected to fail with an [UnsupportedOperationException]
    /// because the specified method is marked as unsupported.
    ///
    /// @param <X>         the type of the test contract
    /// @param unsupported the optional method that is unsupported
    /// @param test        the test method to call, which is expected to fail
    /// @param description a description for the dynamic test
    /// @return a dynamic test instance
    @SuppressWarnings("unchecked")
    protected <X> DynamicTest passesWhenUnsupported(final @NonNull InterfaceMethod unsupported, final @NonNull Consumer<X> test, final @NonNull String description) {
        return dynamicTest(description, () -> assertDoesNotThrow(() -> test.accept((X) createTest(unsupported))));
    }

    /// Creates a dynamic test that is expected to fail with an exception other than
    /// [UnsupportedOperationException] when both an unsupported method and a break are involved.
    ///
    /// This is typically used to verify that a [Break] takes precedence or causes a different
    /// failure even when a method is marked as unsupported.
    ///
    /// @param <X>         the type of the test contract
    /// @param unsupported the optional method that is unsupported
    /// @param aBreak      the break to apply to the test object
    /// @param test        the test method to call, which is expected to fail with an unexpected exception
    /// @param description a description for the dynamic test
    /// @return a dynamic test instance
    @SuppressWarnings("unchecked")
    protected <X> DynamicTest failsWithUnsupportedBreak(final @NonNull InterfaceMethod unsupported, final @NonNull Break aBreak, final @NonNull Consumer<X> test, final @NonNull String description) {
        return dynamicTest(description, () -> assertThrowsDifferent(UnsupportedOperationException.class,
                () -> test.accept((X) createTest(aBreak, unsupported))));
    }

    /// Creates an instance of the test contract with the specified break and optional method status.
    ///
    /// @param b the break to apply (may be null)
    /// @param m the optional method to configure (may be null)
    /// @return an instance of the test contract
    protected abstract @NonNull DynamicContract<?,?> createTest(final Break b, final InterfaceMethod m);

    /// Creates an instance of the test contract with the specified break.
    ///
    /// @param b the break to apply
    /// @return an instance of the test contract
    protected final @NonNull DynamicContract<?,?> createTest(final @NonNull Break b) {
        return createTest(b, null);
    }

    /// Creates an instance of the test contract with the specified optional method status.
    ///
    /// @param m the optional method to configure
    /// @return an instance of the test contract
    protected final @NonNull DynamicContract<?,?> createTest(final @NonNull InterfaceMethod m) {
        return createTest(null, m);
    }

}
