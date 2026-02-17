package org.soliscode.test.contract.sequencedcollection;

import org.soliscode.test.contract.collection.CollectionContract;

import java.util.SequencedCollection;


/// Contract for testing the [SequencedCollection] interface.
///
/// ### Purpose
/// This is a comprehensive contract that combines all individual [SequencedCollection] contracts.
/// It verifies the behavior of navigation methods, view methods, and modification methods
/// as specified in the `SequencedCollection` interface.
///
/// ### Usage Example
/// To use this contract, create a test class that implements it and provides the necessary providers:
/// ```java
/// class MySequencedCollectionTest implements SequencedCollectionContract<String, MySequencedCollection<String>>,
///                                            WithStringElement {
///     @Override
///     public SequencedCollectionProvider<String, MySequencedCollection<String>> provider() {
///         return MySequencedCollection::new;
///     }
/// }
/// ```
///
/// ### Thread Safety
/// The tests in this contract are not thread-safe and should be run in a single-threaded environment
/// unless the underlying collection implementation specifically guarantees thread safety.
///
/// @param <E> The element type.
/// @param <C> The collection type being tested.
/// @author evanbergstrom
/// @see SequencedCollection
/// @since 1.0.0
public interface SequencedCollectionContract<E, C extends SequencedCollection<E>>
        extends CollectionContract<E, C>,
        AddFirstContract<E, C>,
        AddLastContract<E, C>,
        GetFirstContract<E, C>,
        GetLastContract<E, C>,
        RemoveFirstContract<E, C>,
        RemoveLastContract<E, C>,
        ReversedContract<E, C> {

    /// Configures the contract to expect that modification methods are not supported.
    ///
    /// This is a convenience method that calls `doesNotSupportMethod` for all modification
    /// methods in `Collection` and `SequencedCollection`.
    ///
    /// @since 1.0.0
    default void doesNotSupportModification() {
        CollectionContract.super.doesNotSupportModification();
        doesNotSupportMethod(SequencedCollectionMethods.ADD_FIRST);
        doesNotSupportMethod(SequencedCollectionMethods.ADD_LAST);
        doesNotSupportMethod(SequencedCollectionMethods.REMOVE_FIRST);
        doesNotSupportMethod(SequencedCollectionMethods.REMOVE_LAST);
    }
}
