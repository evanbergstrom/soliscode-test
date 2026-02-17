/*
 * Copyright 2024 Evan Bergstrom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.soliscode.test.contract.iterable;

import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.contract.object.ObjectContract;

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
///     public CollectionProvider<String, MyIterable<String>> provider() {
///         return MyIterable::new;
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the [Iterable] and [org.soliscode.test.provider.CollectionProvider] implementations being tested.
///
/// @param <E> The element type being tested.
/// @param <I> The type of the iterable being tested.
/// @author evanbergstrom
/// @see IteratorMethodContract
/// @see ForEachMethodContract
/// @see SpliteratorMethodContract
/// @since 1.0.0
public interface IterableContract<E, I extends Iterable<E>>
    extends ObjectContract<I>,
        IteratorMethodContract<E, I>,
        ForEachMethodContract<E, I>,
        SpliteratorMethodContract<E, I> {

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
