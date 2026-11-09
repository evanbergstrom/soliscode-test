package com.soliscode.test.contract.support;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.soliscode.test.contract.CollectionContractConfig;

/// The base interface for all classes that test `Collection` methods. It allows the contract class to create
/// collections and elements using the associated providers.
///
/// @param <C> The collection type being tested.
/// @param <E> The element type being tested.
/// @author evanbergstrom
/// @since 1.0
public interface CollectionContractSupport<C extends Iterable<E>, E>
        extends ContractSupport<C>, CollectionProviderSupport<C, E>, ElementProviderSupport<E>,
            CollectionContractConfig {

    /// The default number of elements ({@value}) to use for a test.
    int DEFAULT_SIZE = 10;

    /// The most elements ({@value}) a test traverses before it fails: an iterator or spliterator that does not end
    /// must fail the test, not hang it.
    int TRAVERSAL_LIMIT = 10_000;

    /// Asserts that a query with an incompatible type gives the expected result, or, for a collection that does not
    /// permit incompatible types, throws [ClassCastException]: [java.util.Collection] makes that exception optional
    /// for `contains`, `containsAll`, `remove`, `removeAll` and `retainAll`.
    ///
    /// @param expected the result expected when no exception is thrown
    /// @param query the query
    /// @param description what the query is, for the failure message
    default void assertResultOrClassCast(final boolean expected, final java.util.function.BooleanSupplier query,
                                         final String description) {
        try {
            org.junit.jupiter.api.Assertions.assertEquals(expected, query.getAsBoolean(), description);
        } catch (final ClassCastException e) {
            if (permitIncompatibleTypes()) {
                throw e;
            }
        }
    }

    /// Asserts that a query with a `null` argument gives the expected result, or, for a collection that does not
    /// permit `null`, throws [NullPointerException]: [java.util.Collection] makes that exception optional for
    /// `contains`, `containsAll`, `remove`, `removeAll` and `retainAll` (`ArrayBlockingQueue.contains(null)`, for
    /// one, returns `false`).
    ///
    /// @param expected the result expected when no exception is thrown
    /// @param query the query
    /// @param description what the query is, for the failure message
    /// @since 0.1.2
    default void assertResultOrNullPointer(final boolean expected, final java.util.function.BooleanSupplier query,
                                           final String description) {
        try {
            org.junit.jupiter.api.Assertions.assertEquals(expected, query.getAsBoolean(), description);
        } catch (final NullPointerException e) {
            if (permitNulls()) {
                throw e;
            }
        }
    }

    /// Returns the elements in the order a sequenced collection holding them should encounter them: in the order of
    /// its comparator, for a [SortedSet], otherwise in the order they were given.
    ///
    /// [java.util.SequencedCollection] allows any defined encounter order. A test of a collection whose order is
    /// neither overrides this method to give that order. A sorted set's expected order comes from its own
    /// `comparator()`, as the sorted maps' does ([OrderedMapContractSupport#encounterOrder]); the comparator's own
    /// test checks that it describes the order.
    ///
    /// @param collection the collection under test
    /// @param elements the elements it holds, in the order they were added
    /// @return the elements in encounter order
    default List<E> encounterOrder(final Iterable<E> collection, final List<E> elements) {
        if (collection instanceof SortedSet<E> sorted) {
            TreeSet<E> reference = new TreeSet<>(sorted.comparator());
            reference.addAll(elements);
            return new ArrayList<>(reference);
        }
        return elements;
    }
}
