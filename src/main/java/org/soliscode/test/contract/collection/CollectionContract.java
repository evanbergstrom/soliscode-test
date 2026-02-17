package org.soliscode.test.contract.collection;

import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.contract.CollectionContractConfig;
import org.soliscode.test.contract.iterable.IterableContract;

import java.util.Collection;

/// **Contract for the `Collection` interface**
///
/// This interface defines a comprehensive test suite for classes that implement the [Collection]
/// interface. It is designed to be used as a mix-in interface by test classes that verify
/// [Collection] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a collection implementation correctly:
/// - Implements all [Collection] methods according to their specifications.
/// - Handles configuration for nulls, duplicates, and incompatible types.
/// - Supports marking methods as unsupported.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class and configure it appropriately:
///
/// ```java
/// class MyCollectionTest implements CollectionContract<Integer, MyCollection<Integer>> {
///     public MyCollectionTest() {
///         permitNulls(true);
///         permitDuplicates(true);
///         permitIncompatibleTypes(true);
///     }
///
///     @Override
///     public CollectionProvider<Integer, MyCollection<Integer>> provider() {
///         return MyCollection::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the [Collection] and [org.soliscode.test.provider.CollectionProvider] implementations being tested.
///
/// @param <E> The element type being tested.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see Collection
/// @since 1.0.0
public interface CollectionContract<E, C extends Collection<E>> extends IterableContract<E, C>,
        CollectionContractConfig,
        AddContract<E, C>,
        AddAllContract<E, C>,
        ClearContract<E, C>,
        ContainsContract<E, C>,
        ContainsAllContract<E, C>,
        IsEmptyContract<E, C>,
        RemoveContract<E, C>,
        RemoveAllContract<E, C>,
        RemoveIfContract<E, C>,
        RetainAllContract<E, C>,
        SizeContract<E, C>,
        StreamContract<E, C>,
        ToArrayContract<E, C> {

    @Override
    boolean supportsMethod(InterfaceMethod method);

    /// Configures the contract to not expect support for all modification methods.
    ///
    /// This is a convenience method to disable tests for:
    /// - [add(Object)][Collection#add]
    /// - [addAll(Collection)][Collection#addAll]
    /// - [clear()][Collection#clear]
    /// - [remove(Object)][Collection#remove]
    /// - [removeAll(Collection)][Collection#removeAll]
    /// - [removeIf(Predicate)][Collection#removeIf]
    /// - [retainAll(Collection)][Collection#retainAll]
    /// - [Iterator][java.util.Iterator]#remove
    ///
    /// @since 1.0.0
    default void doesNotSupportModification() {
        IterableContract.super.doesNotSupportModification();
        doesNotSupportMethod(CollectionMethods.ADD);
        doesNotSupportMethod(CollectionMethods.ADD_ALL);
        doesNotSupportMethod(CollectionMethods.CLEAR);
        doesNotSupportMethod(CollectionMethods.REMOVE);
        doesNotSupportMethod(CollectionMethods.REMOVE_ALL);
        doesNotSupportMethod(CollectionMethods.REMOVE_IF);
        doesNotSupportMethod(CollectionMethods.RETAIN_ALL);
    }
}
