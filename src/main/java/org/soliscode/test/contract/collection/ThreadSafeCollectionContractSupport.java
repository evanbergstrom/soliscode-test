package org.soliscode.test.contract.collection;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.contract.support.CollectionContractSupport;

import java.util.Collection;

import static org.soliscode.test.assertions.collection.CollectionAssertions.assertContains;

/// Support interface for thread-safe collection contracts.
///
/// This interface provides common assertions and utilities for testing the thread safety
/// of [Collection] implementations.
///
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @since 1.0.0
public interface ThreadSafeCollectionContractSupport<C extends Collection<E>, E>
        extends CollectionContractSupport<C, E> {

    /// Asserts that a collection's size agrees with its elements, and that it holds no `null`: what a race between
    /// writers can break.
    ///
    /// @param collection the collection to check
    default void assertConsistent(final @NonNull C collection) {
        int count = 0;
        for (E e : collection) {
            org.junit.jupiter.api.Assertions.assertNotNull(e, "the collection holds a null that was never added");
            count++;
        }
        org.junit.jupiter.api.Assertions.assertEquals(count, collection.size(),
                "size() disagrees with the elements");
    }

    /// Asserts that the collection is still functional after concurrent operations.
    ///
    /// This method attempts to add an element to the collection and verifies that it
    /// was added successfully.
    ///
    /// @param collection the collection to check
    /// @since 1.0.0
    default void assertCollectionStillWorks(final @NonNull C collection) {
        E e = elementProvider().createInstance();
        collection.add(e);
        assertContains(e, collection);
    }
}
