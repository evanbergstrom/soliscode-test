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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.assertions.actions.AssertActions;
import org.soliscode.test.assertions.actions.AssertConsumeCount;
import org.soliscode.test.assertions.actions.AssertConsumeExactly;
import org.soliscode.test.contract.support.CollectionContractSupport;
import org.soliscode.test.provider.CollectionProvider;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertThrows;

/// **Contract for the `forEach` method of an `Iterable`**
///
/// This interface defines tests for the [forEach(Consumer)][Iterable#forEach] method. It is designed
/// to be used as a mix-in interface by test classes that verify [Iterable] implementations.
///
/// ## Purpose
/// The purpose of this contract is to ensure that an iterable's `forEach` implementation correctly:
/// - Calls the specified action for each element in the iterable.
/// - Does not call the action for an empty iterable.
/// - Throws [NullPointerException] if the action is `null`.
///
/// ## Usage Examples
/// To use this contract, implement it in your test class along with the required support interfaces:
///
/// ```java
/// class MyIterableForEachTest implements ForEachMethodContract<String, MyIterable<String>> {
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
/// @see Iterable#forEach(Consumer)
/// @since 1.0.0
public interface ForEachMethodContract<E, I extends Iterable<E>> extends CollectionContractSupport<E, I> {

    /// Tests that the [forEach][Iterable#forEach] method works for an empty collection.
    ///
    /// This test verifies that the action is not called for an empty collection.
    ///
    /// @since 1.0.0
    @Test
    @DisplayName("forEach(Consumer) does not call the action for an iterable with no elements")
    default void forEach_whenEmpty_doesNotCallAction() {
        Iterable<E> iterable = provider().emptyInstance();
        AssertConsumeCount<E> action = AssertActions.consumeCount(0);
        iterable.forEach(action);
        action.assertCheck();
    }

    /// Tests that the [forEach][Iterable#forEach] method works for a collection with elements.
    ///
    /// This test verifies that the action is called exactly once for each element in the collection.
    ///
    /// @since 1.0.0
    @Test
    @DisplayName("forEach(Consumer) calls the action for each element in an iterable")
    default void forEach_whenNotEmpty_callsActionForEachElement() {

        CollectionProvider<E, I> provider = provider();
        Iterable<E> iterable = provider.createInstanceWithUniqueElements();
        AssertConsumeExactly<E> action = AssertActions.consumeExactly(iterable);
        iterable.forEach(action);
        action.assertCheck();
    }

    /// Tests that the [forEach][Iterable#forEach] method throws for a `null` action.
    ///
    /// @since 1.0.0
    @Test
    @DisplayName("forEach(Consumer) throws NullPointerException when called with a null action")
    default void forEach_withNullAction_throwsNullPointerException() {
        Iterable<E> iterable = provider().createInstanceWithUniqueElements();
        Consumer<E> action = null;
        //noinspection ConstantValue
        assertThrows(NullPointerException.class, () -> iterable.forEach(action));
    }
}
