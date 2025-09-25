package org.soliscode.test.contract.list;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.DoesNotPermitIncompatibleTypes;
import org.soliscode.test.contract.DoesNotPermitNulls;
import org.soliscode.test.contract.support.WithLinkedList;
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.provider.*;
import org.soliscode.test.util.CollectionTestUtils;

import java.util.*;

/**
 * Comprehensive test suite for validating {@link LinkedList} implementations against the {@link List} contract.
 * 
 * <p>This test class demonstrates how to use the SolisCode Test framework to thoroughly validate a LinkedList
 * implementation. It includes tests for the standard modifiable LinkedList as well as several variations
 * that test different behavioral constraints.
 * 
 * <p>The main test class validates a standard {@link LinkedList} with Integer elements that:
 * <ul>
 * <li>Supports all standard List operations (add, remove, get, set, etc.)</li>
 * <li>Allows null values</li>
 * <li>Allows duplicate values</li>
 * <li>Maintains insertion order</li>
 * <li>Is modifiable (supports add/remove operations)</li>
 * <li>Provides efficient insertion and deletion at both ends</li>
 * <li>Implements Deque interface for double-ended queue operations</li>
 * </ul>
 * 
 * <p>LinkedList differs from ArrayList in its internal structure and performance characteristics:
 * <ul>
 * <li>Uses a doubly-linked list structure rather than a dynamic array</li>
 * <li>Provides O(1) insertion/deletion at ends, O(n) at arbitrary positions</li>
 * <li>Has O(n) random access time compared to ArrayList's O(1)</li>
 * <li>Uses more memory per element due to node overhead</li>
 * </ul>
 * 
 * <p>Additionally, this class contains nested test classes that validate LinkedList behavior under
 * different constraints:
 * <ul>
 * <li>{@link UnmodifiableLinkedListTest} - Tests unmodifiable wrapper behavior</li>
 * <li>{@link NoNullsLinkedListContract} - Tests null-rejecting wrapper behavior</li>
 * <li>{@link CheckedLinkedListContract} - Tests type-checked wrapper behavior</li>
 * </ul>
 * 
 * <p>Usage example:
 * <pre>{@code
 * // Run all LinkedList contract tests
 * mvn test -Dtest=LinkedListTest
 * 
 * // Run only the unmodifiable tests
 * mvn test -Dtest=LinkedListTest$UnmodifiableLinkedListTest
 * }</pre>
 * 
 * @author evanbergstrom
 * @see ListContract
 * @see LinkedList
 * @see AbstractTest
 * @since 1.0
 */
@DisplayName("Test the TestList interface using a LinkedList")
public class LinkedListTest extends AbstractTest
        implements ListContract<Integer, LinkedList<Integer>>, WithLinkedList<Integer>, WithIntegerElement {

    /**
     * Constructs a new LinkedListTest with default configuration.
     * 
     * <p>The default LinkedList configuration permits:
     * <ul>
     * <li>Null values (inherited from WithIntegerElement)</li>
     * <li>Duplicate values (standard List behavior)</li>
     * <li>All modification operations</li>
     * <li>Double-ended queue operations (addFirst, addLast, etc.)</li>
     * </ul>
     */
    public LinkedListTest() {
    }

    /**
     * Tests LinkedList wrapped with {@link Collections#unmodifiableList(List)} to verify proper
     * handling of modification attempts.
     * 
     * <p>This nested test class validates that unmodifiable List wrappers correctly throw
     * {@link UnsupportedOperationException} for all modification operations while still
     * supporting read-only operations like get, size, and iteration.
     * 
     * <p>The test automatically configures the framework to expect modification operations
     * to fail by calling {@link ListContract#doesNotSupportModification()}.
     */
    @Nested
    @DisplayName("Test the TestList interface using a LinkedList that is unmodifiable")
    public class UnmodifiableLinkedListTest extends AbstractTest
            implements ListContract<Integer, List<Integer>>, WithIntegerElement {

        /**
         * Constructs a new UnmodifiableLinkedListTest and configures it to expect
         * modification operations to throw {@link UnsupportedOperationException}.
         */
        public UnmodifiableLinkedListTest() {
            doesNotSupportModification();
        }

        @Override
        public @NonNull CollectionProvider<Integer, List<Integer>> provider() {
            return CollectionProviders.wrap(CollectionProviders.provideLinkedList(Providers.integerProvider()),
                    Collections::unmodifiableList);
        }
    }

    /**
     * Tests LinkedList with a wrapper that rejects null values to verify proper null handling.
     * 
     * <p>This nested test class validates that List implementations correctly handle null rejection
     * by throwing {@link NullPointerException} when null values are added. The wrapper is applied
     * using {@link CollectionTestUtils#preventNulls(Collection)} which decorates the LinkedList
     * to reject nulls.
     * 
     * <p>The test implements {@link DoesNotPermitNulls} to inform the contract framework
     * that null-related operations should throw exceptions rather than succeed.
     */
    @Nested
    @DisplayName("Test the TestList interface using a LinkedList that does not accept nulls")
    public class NoNullsLinkedListContract extends AbstractTest
            implements ListContract<Integer, List<Integer>>, WithIntegerElement, DoesNotPermitNulls {

        @Override
        public @NonNull CollectionProvider<Integer, List<Integer>> provider() {
            return CollectionProviders.wrap(CollectionProviders.provideLinkedList(Providers.integerProvider()),
                    CollectionTestUtils::preventNulls);
        }
    }

    /**
     * Tests LinkedList wrapped with {@link Collections#checkedList(List, Class)} to verify proper
     * type checking at runtime.
     * 
     * <p>This nested test class validates that type-checked List wrappers correctly throw
     * {@link ClassCastException} when incompatible types are added. The wrapper ensures
     * type safety by checking that all elements are instances of the specified class.
     * 
     * <p>The test implements {@link DoesNotPermitIncompatibleTypes} to inform the contract
     * framework that operations with incompatible types should throw {@link ClassCastException}
     * rather than succeed.
     */
    @Nested
    @DisplayName("Test the TestList interface using a LinkedList that does not permit incompatible types")
    public class CheckedLinkedListContract extends AbstractTest
            implements ListContract<Integer, List<Integer>>, WithIntegerElement, DoesNotPermitIncompatibleTypes {

        @Override
        public @NonNull CollectionProvider<Integer, List<Integer>> provider() {
            return CollectionProviders.wrap(CollectionProviders.provideLinkedList(Providers.integerProvider()),
                    (c) -> Collections.checkedList(c, Integer.class));
        }
    }
}