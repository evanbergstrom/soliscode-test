package org.soliscode.test.contract.set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.contract.collection.CollectionContract;
import org.soliscode.test.contract.object.ObjectMethods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Test suite for classes that implement the {@link Set} interface.
///
/// This contract extends {@link CollectionContract} and is intended for testing implementations
/// of the `Set` interface. While the `Set` interface does not add_singleElement_returnsTrueAndUpdatesSize any new methods beyond those
/// defined in {@link java.util.Collection}, it imposes additional stipulations on the constructors
/// and the behavior of the `add_singleElement_returnsTrueAndUpdatesSize`, `equals`, and `hashCode` methods.
///
/// When implementing this contract, the only method that needs to be implemented is
/// [objectProvider()][org.soliscode.test.contract.support.CollectionContractSupport#provider()].
///
/// ## Usage Example
///
/// To create a test class for `HashSet`, the following implementation would be appropriate:
///
/// ```java
/// public class HashSetTest
///         extends AbstractTest<Integer>
///         implements SetContract<Integer, HashSet<Integer>>, WithIntegerElement<HashSet<Integer>> {
///
///     public HashSetTest() {
///         permitNulls(true);
///         permitIncompatibleTypes(true);
///         // Sets do not permit duplicates by definition,
///         // so permitDuplicates(true) would violate Set contract.
///         permitDuplicates(false);
///     }
///
///     @Override
///     public CollectionProvider<Integer, HashSet<Integer>> objectProvider() {
///         return CollectionProviders.provideHashSet(elementProvider());
///     }
/// }
/// ```
///
/// ## Thread Safety
///
/// Implementations of this contract are expected to be thread-safe for use by the JUnit test runner.
/// The tested `Set` instance itself should be handled according to its own thread safety guarantees.
///
/// @param <E> The element type being tested.
/// @param <S> The set type being tested.
/// @author evanbergstrom
/// @see Set
/// @since 1.0
public interface SetContract<S extends Set<E>, E> extends CollectionContract<S, E> {

    @Override
    boolean supportsMethod(InterfaceMethod method);

    @Override
    void doesNotSupportMethod(InterfaceMethod method);

    /// Tests that the set's spliterator reports [DISTINCT][Spliterator#DISTINCT], as [Set#spliterator()] requires.
    @Test
    @DisplayName("spliterator() reports DISTINCT")
    default void spliterator_whenCalled_reportsDistinct() {
        Set<E> set = provider().createInstanceWithUniqueElements();
        assertTrue(set.spliterator().hasCharacteristics(Spliterator.DISTINCT));
    }

    /// Tests that [equals(Object)][Set#equals(Object)] is true for any set with the same elements, whatever its
    /// order, as [Set#equals(Object)] requires.
    @Test
    @DisplayName("equals(Object) is true for a set with the same elements in another order")
    default void equals_withSameElementsInOtherOrder_returnsTrue() {
        if (supportsMethod(ObjectMethods.EQUALS)) {
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            Set<E> set = provider().createInstance(values);
            List<E> reversed = new ArrayList<>(values);
            Collections.reverse(reversed);
            Set<E> other = new HashSet<>(reversed);
            assertEquals(other, set);
            assertEquals(set, other);
        }
    }

    /// Tests that [hashCode()][Set#hashCode()] is the sum of the elements' hash codes, as [Set#hashCode()] requires.
    @Test
    @DisplayName("hashCode() is the sum of the elements' hash codes")
    default void hashCode_whenCalled_isSumOfElementHashCodes() {
        if (supportsMethod(ObjectMethods.HASH_CODE)) {
            List<E> values = elementProvider().createUniqueInstances(DEFAULT_SIZE);
            Set<E> set = provider().createInstance(values);
            assertEquals(values.stream().mapToInt(Objects::hashCode).sum(), set.hashCode());
        }
    }
}
