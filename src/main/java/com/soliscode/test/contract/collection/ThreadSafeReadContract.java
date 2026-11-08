package com.soliscode.test.contract.collection;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.junit.jupiter.api.DisplayName;

import com.soliscode.test.annotations.ConcurrencyTest;
import com.soliscode.test.annotations.VerySlow;
import com.soliscode.test.safety.ContentionCoordinator;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Thread-safety contract for the methods that read a collection while other threads change it:
/// [contains(Object)][Collection#contains], [containsAll(Collection)][Collection#containsAll],
/// [toArray()][Collection#toArray()] and [toArray(Object[])][Collection#toArray(Object[])].
///
/// Other threads add and remove elements, which moves the rest; a read that is not thread safe can then miss an
/// element that is there throughout, or see an element that is not there (a `null`).
///
/// @param <C> the type of collection
/// @param <E> the type of elements
/// @since 1.0
public interface ThreadSafeReadContract<C extends Collection<E>, E> extends ThreadSafeCollectionContractSupport<C, E> {

    /// How many elements ({@value}) are ahead of the one the contains test looks for, and are removed under it.
    int CONTAINS_PRECEDING_ELEMENTS = 2_000;

    /// Tests that [contains(Object)][Collection#contains] and [containsAll(Collection)][Collection#containsAll] find
    /// an element that is in the collection throughout, while other threads add and remove other elements.
    @DisplayName("contains(Object) and containsAll(Collection) are thread safe under contention with add and remove")
    @VerySlow
    @ConcurrencyTest
    default void containsWhenCalledUnderContentionWithAddAndRemoveIsThreadSafe() {
        if (supportsMethod(CollectionMethods.ADD) && supportsMethod(CollectionMethods.REMOVE)) {
            Supplier<E> supplier = elementProvider().uniqueInstanceSupplier();
            C collection = provider().emptyInstance();
            // Many elements ahead of the permanent one: each removal moves the elements behind it, the permanent one
            // included, which is when a read that is not thread safe can step past it.
            java.util.Queue<E> toRemove = new java.util.concurrent.ConcurrentLinkedQueue<>();
            for (int i = 0; i < CONTAINS_PRECEDING_ELEMENTS; i++) {
                E e = supplier.get();
                toRemove.add(e);
                collection.add(e);
            }
            E permanent = supplier.get();
            collection.add(permanent);
            java.util.List<E> permanentOnly = java.util.List.of(permanent);

            ContentionCoordinator<C> coordinator = new ContentionCoordinator<>();
            coordinator.add((c) -> {
                E e = toRemove.poll();
                if (e != null) {
                    c.remove(e);
                }
            });
            coordinator.add((c) -> assertTrue(c.contains(permanent), "contains(Object) missed an element"));
            coordinator.add((c) -> assertTrue(c.containsAll(permanentOnly), "containsAll(Collection) missed it"));
            coordinator.execute(collection);
            assertCollectionStillWorks(collection);
        }
    }

    /// Tests that [toArray()][Collection#toArray()] and [toArray(Object[])][Collection#toArray(Object[])] return only
    /// elements that were added, never a `null`, while other threads add and remove elements.
    @DisplayName("toArray() and toArray(T[]) are thread safe under contention with add and remove")
    @VerySlow
    @ConcurrencyTest
    default void toArrayWhenCalledUnderContentionWithAddAndRemoveIsThreadSafe() {
        if (supportsMethod(CollectionMethods.ADD) && supportsMethod(CollectionMethods.REMOVE)) {
            Supplier<E> supplier = elementProvider().uniqueInstanceSupplier();
            Set<E> added = ConcurrentHashMap.newKeySet();
            C collection = provider().emptyInstance();
            for (int i = 0; i < DEFAULT_SIZE; i++) {
                E e = supplier.get();
                added.add(e);
                collection.add(e);
            }

            ContentionCoordinator<C> coordinator = new ContentionCoordinator<>();
            coordinator.add((c) -> {
                E e = supplier.get();
                added.add(e);
                c.add(e);
                c.remove(e);
            });
            coordinator.add((c) -> checkElements(c.toArray(), added));
            coordinator.add((c) -> checkElements(c.toArray(new Object[0]), added));
            coordinator.execute(collection);
            assertCollectionStillWorks(collection);
        }
    }

    private static <E> void checkElements(final Object[] elements, final Set<E> added) {
        for (final Object e : elements) {
            assertNotNull(e, "toArray returned a null that was never added");
            assertTrue(added.contains(e), "toArray returned an element that was never added");
        }
    }
}
