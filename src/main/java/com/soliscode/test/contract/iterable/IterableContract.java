/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.iterable;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.contract.object.ObjectContract;
import com.soliscode.test.methods.IterableMethods;

/// **Contract for the `Iterable` interface**
///
/// This interface tests if an iterable class has implemented the [Iterable] methods correctly.
/// It is designed to be used as a mix-in interface by test classes that verify [Iterable]
/// implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that an implementation of [Iterable]:
/// - Provides a valid iterator via [iterator()][Iterable#iterator].
/// - Correctly implements [forEach(Consumer)][Iterable#forEach].
/// - Provides a valid spliterator via [spliterator()][Iterable#spliterator].
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyIterableContractTest implements IterableContract<String, MyIterable<String>> {
///     @Override
///     public CollectionProvider<String, MyIterable<String>> objectProvider() {
///         return MyIterable::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the [Iterable] and [com.soliscode.test.provider.CollectionProvider] implementations being tested.
///
/// @param <E> The element type being tested.
/// @param <I> The type of the iterable being tested.
/// @author evanbergstrom
/// @see IteratorMethodContract
/// @see ForEachMethodContract
/// @see SpliteratorMethodContract
/// @since 1.0.0
public interface IterableContract<I extends Iterable<E>, E>
    extends ObjectContract<I>,
        IteratorMethodContract<I, E>,
        ForEachMethodContract<I, E>,
        SpliteratorMethodContract<I, E> {

    /// Indicates that the class being tested does not support an optional method.
    ///
    /// @param method the method that is not supported.
    /// @since 1.0.0
    void doesNotSupportMethod(InterfaceMethod method);

    /// Configures the contract to not expect support for modification methods (specifically iterator remove).
    ///
    /// @since 1.0.0
    default void doesNotSupportModification() {
        doesNotSupportMethod(IterableMethods.ITERATOR_REMOVE);
    }
}
