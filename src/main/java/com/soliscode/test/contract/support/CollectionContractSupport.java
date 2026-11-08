package com.soliscode.test.contract.support;

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
}
