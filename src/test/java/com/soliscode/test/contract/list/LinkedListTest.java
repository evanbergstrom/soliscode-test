package com.soliscode.test.contract.list;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.contract.DoesNotPermitIncompatibleTypes;
import com.soliscode.test.contract.DoesNotPermitNulls;
import com.soliscode.test.contract.support.WithIntegerElement;
import com.soliscode.test.contract.support.WithLinkedList;
import com.soliscode.test.provider.CollectionProvider;
import com.soliscode.test.provider.CollectionProviders;
import com.soliscode.test.provider.Providers;
import com.soliscode.test.util.CollectionTestUtils;

/// Comprehensive test suite for validating [LinkedList] implementations against the [List] contract.
///
/// This test class demonstrates how to use the SolisCode Test framework to thoroughly validate a LinkedList
/// implementation. It includes tests for the standard modifiable LinkedList as well as several variations
/// that test different behavioral constraints.
///
/// The main test class validates a standard [LinkedList] with Integer elements that:
///
///   - Supports all standard List operations (add, remove, get, set, etc.)
///   - Allows null values
///   - Allows duplicate values
///   - Maintains insertion order
///   - Is modifiable (supports add/remove operations)
///   - Provides efficient insertion and deletion at both ends
///   - Implements Deque interface for double-ended queue operations
///
///
/// LinkedList differs from ArrayList in its internal structure and performance characteristics:
///
///   - Uses a doubly-linked list structure rather than a dynamic array
///   - Provides O(1) insertion/deletion at ends, O(n) at arbitrary positions
///   - Has O(n) random access time compared to ArrayList's O(1)
///   - Uses more memory per element due to node overhead
///
///
/// Additionally, this class contains nested test classes that validate LinkedList behavior under
/// different constraints:
///
///   - [UnmodifiableLinkedListTest] - Tests unmodifiable wrapper behavior
///   - [NoNullsLinkedListContract] - Tests null-rejecting wrapper behavior
///   - [CheckedLinkedListContract] - Tests type-checked wrapper behavior
///
///
/// Usage example:
/// ```
/// // Run all LinkedList contract tests
/// mvn test -Dtest=LinkedListTest
/// // Run only the unmodifiable tests
/// mvn test -Dtest=LinkedListTest$UnmodifiableLinkedListTest
/// ```
///
/// @author evanbergstrom
/// @see ListContract
/// @see LinkedList
/// @see AbstractTest
/// @since 1.0
@DisplayName("Test the TestList interface using a LinkedList")
public class LinkedListTest extends AbstractTest
        implements ListContract<LinkedList<Integer>, Integer>, WithLinkedList<Integer>, WithIntegerElement {

    /// Constructs a new LinkedListTest with default configuration.
    ///
    /// The default LinkedList configuration permits:
    ///
    ///   - Null values (inherited from WithIntegerElement)
    ///   - Duplicate values (standard List behavior)
    ///   - All modification operations
    ///   - Double-ended queue operations (addFirst, addLast, etc.)
    ///
    public LinkedListTest() {
    }

    /// Tests LinkedList wrapped with [Collections#unmodifiableList(List)] to verify proper
    /// handling of modification attempts.
    ///
    /// This nested test class validates that unmodifiable List wrappers correctly throw
    /// [UnsupportedOperationException] for all modification operations while still
    /// supporting read-only operations like get, size, and iteration.
    ///
    /// The test automatically configures the framework to expect modification operations
    /// to fail by calling [ListContract#doesNotSupportModification()].
    @Nested
    @DisplayName("Test the TestList interface using a LinkedList that is unmodifiable")
    public class UnmodifiableLinkedListTest extends AbstractTest
            implements ListContract<List<Integer>, Integer>, WithIntegerElement {

        /// Constructs a new UnmodifiableLinkedListTest and configures it to expect
        /// modification operations to throw [UnsupportedOperationException].
        public UnmodifiableLinkedListTest() {
            doesNotSupportModification();
        }

        @Override
        public CollectionProvider<List<Integer>, Integer> provider() {
            return CollectionProviders.wrap(CollectionProviders.provideLinkedList(Providers.integerProvider()),
                    Collections::unmodifiableList);
        }
    }

    /// Tests LinkedList with a wrapper that rejects null values to verify proper null handling.
    ///
    /// This nested test class validates that List implementations correctly handle null rejection
    /// by throwing [NullPointerException] when null values are added. The wrapper is applied
    /// using [CollectionTestUtils#preventNulls(Collection)] which decorates the LinkedList
    /// to reject nulls.
    ///
    /// The test implements [DoesNotPermitNulls] to inform the contract framework
    /// that null-related operations should throw exceptions rather than succeed.
    @Nested
    @DisplayName("Test the TestList interface using a LinkedList that does not accept nulls")
    public class NoNullsLinkedListContract extends AbstractTest
            implements ListContract<List<Integer>, Integer>, WithIntegerElement, DoesNotPermitNulls {

        /// Default constructor.
        public NoNullsLinkedListContract() {
            super();
        }

        @Override
        public CollectionProvider<List<Integer>, Integer> provider() {
            return CollectionProviders.wrap(CollectionProviders.provideLinkedList(Providers.integerProvider()),
                    CollectionTestUtils::preventNulls);
        }
    }

    /// Tests LinkedList wrapped with [Collections#checkedList(List, Class)] to verify proper
    /// type checking at runtime.
    ///
    /// This nested test class validates that type-checked List wrappers correctly throw
    /// [ClassCastException] when incompatible types are added. The wrapper ensures
    /// type safety by checking that all elements are instances of the specified class.
    ///
    /// The test implements [DoesNotPermitIncompatibleTypes] to inform the contract
    /// framework that operations with incompatible types should throw [ClassCastException]
    /// rather than succeed.
    @Nested
    @DisplayName("Test the TestList interface using a LinkedList that does not permit incompatible types")
    public class CheckedLinkedListContract extends AbstractTest
            implements ListContract<List<Integer>, Integer>, WithIntegerElement, DoesNotPermitIncompatibleTypes {

        /// Default constructor.
        public CheckedLinkedListContract() {
            super();
        }

        @Override
        public CollectionProvider<List<Integer>, Integer> provider() {
            return CollectionProviders.wrap(CollectionProviders.provideLinkedList(Providers.integerProvider()),
                    (c) -> Collections.checkedList(c, Integer.class));
        }
    }
}
