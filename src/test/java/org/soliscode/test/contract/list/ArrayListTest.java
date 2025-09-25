package org.soliscode.test.contract.list;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.DoesNotPermitIncompatibleTypes;
import org.soliscode.test.contract.DoesNotPermitNulls;
import org.soliscode.test.contract.support.WithArrayList;
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.provider.*;
import org.soliscode.test.util.CollectionTestUtils;

import java.util.*;

/**
 * Comprehensive test suite for validating {@link ArrayList} implementations against the {@link List} contract.
 * 
 * <p>This test class demonstrates how to use the SolisCode Test framework to thoroughly validate a List
 * implementation. It includes tests for the standard modifiable ArrayList as well as several variations
 * that test different behavioral constraints.
 * 
 * <p>The main test class validates a standard {@link ArrayList} with Integer elements that:
 * <ul>
 * <li>Supports all standard List operations (add, remove, get, set, etc.)</li>
 * <li>Allows null values</li>
 * <li>Allows duplicate values</li>
 * <li>Maintains insertion order</li>
 * <li>Is modifiable (supports add/remove operations)</li>
 * </ul>
 * 
 * <p>Additionally, this class contains nested test classes that validate ArrayList behavior under
 * different constraints:
 * <ul>
 * <li>{@link UnmodifiableArrayListTest} - Tests unmodifiable wrapper behavior</li>
 * <li>{@link NoNullsArrayListContract} - Tests null-rejecting wrapper behavior</li>
 * <li>{@link CheckedArrayListContract} - Tests type-checked wrapper behavior</li>
 * </ul>
 * 
 * <p>Usage example:
 * <pre>{@code
 * // Run all ArrayList contract tests
 * mvn test -Dtest=ArrayListTest
 * 
 * // Run only the unmodifiable tests
 * mvn test -Dtest=ArrayListTest$UnmodifiableArrayListTest
 * }</pre>
 * 
 * @author evanbergstrom
 * @see ListContract
 * @see ArrayList
 * @see AbstractTest
 * @since 1.0
 */
@DisplayName("Test the TestList interface using an ArrayList")
public class ArrayListTest extends AbstractTest
        implements ListContract<Integer, ArrayList<Integer>>, WithArrayList<Integer>, WithIntegerElement {

    /**
     * Constructs a new ArrayListTest with default configuration.
     * 
     * <p>The default ArrayList configuration permits:
     * <ul>
     * <li>Null values (inherited from WithIntegerElement)</li>
     * <li>Duplicate values (standard List behavior)</li>
     * <li>All modification operations</li>
     * </ul>
     */
    public ArrayListTest() {
    }

    /**
     * Tests ArrayList wrapped with {@link Collections#unmodifiableList(List)} to verify proper
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
    @DisplayName("Test the TestList interface using an ArrayList that is unmodifiable")
    public class UnmodifiableArrayListTest extends AbstractTest
            implements ListContract<Integer, List<Integer>>, WithIntegerElement {

        /**
         * Constructs a new UnmodifiableArrayListTest and configures it to expect
         * modification operations to throw {@link UnsupportedOperationException}.
         */
        public UnmodifiableArrayListTest() {
            doesNotSupportModification();
        }

        @Override
        public @NonNull CollectionProvider<Integer, List<Integer>> provider() {
            return CollectionProviders.wrap(CollectionProviders.provideArrayList(Providers.integerProvider()),
                    Collections::unmodifiableList);
        }
    }

    /**
     * Tests ArrayList with a wrapper that rejects null values to verify proper null handling.
     * 
     * <p>This nested test class validates that List implementations correctly handle null rejection
     * by throwing {@link NullPointerException} when null values are added. The wrapper is applied
     * using {@link CollectionTestUtils#preventNulls(Collection)} which decorates the ArrayList
     * to reject nulls.
     * 
     * <p>The test implements {@link DoesNotPermitNulls} to inform the contract framework
     * that null-related operations should throw exceptions rather than succeed.
     */
    @Nested
    @DisplayName("Test the TestList interface using an ArrayList that does not accept nulls")
    public class NoNullsArrayListContract extends AbstractTest
            implements ListContract<Integer, List<Integer>>, WithIntegerElement, DoesNotPermitNulls {

        @Override
        public @NonNull CollectionProvider<Integer, List<Integer>> provider() {
            return CollectionProviders.wrap(CollectionProviders.provideArrayList(Providers.integerProvider()),
                    CollectionTestUtils::preventNulls);
        }
    }

    /**
     * Tests ArrayList wrapped with {@link Collections#checkedList(List, Class)} to verify proper
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
    @DisplayName("Test the TestList interface using an ArrayList that does not permit incompatible types")
    public class CheckedArrayListContract extends AbstractTest
            implements ListContract<Integer, List<Integer>>, WithIntegerElement, DoesNotPermitIncompatibleTypes {

        @Override
        public @NonNull CollectionProvider<Integer, List<Integer>> provider() {
            return CollectionProviders.wrap(CollectionProviders.provideArrayList(Providers.integerProvider()),
                    (c) -> Collections.checkedList(c, Integer.class));
        }
    }
}