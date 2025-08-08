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
import org.soliscode.test.assertions.actions.AssertConsumer;
import org.soliscode.test.contract.support.CollectionContractSupport;
import org.soliscode.test.provider.CollectionProvider;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertThrows;

/// This interface tests if a collection class has implemented the `forEach()` method correctly based upon the
/// specification provided in [Iterable#forEach(Consumer)].
///
/// @param <E> The element type being tested.
/// @param <I> The iterator type being tested.
///
/// @author evanbergstrom
/// @see Iterable#forEach(Consumer)
/// @since 1.0
public interface ForEachContract<E, I extends Iterable<E>> extends CollectionContractSupport<E, I> {

    /// Tests that the [Iterable#forEach] works for an empty collection.
    @Test
    @DisplayName("ForEach does not call the action for an iterable with no elements")
    default void testForEachForEmptyCollection() {
        Iterable<E> iterable = provider().emptyInstance();
        AssertConsumer<E> action = AssertActions.consumeCount(0);
        iterable.forEach(action);
        action.assertCheck();
    }

    /// Tests that the `forEach()` works for a collection with elements.
    @Test
    @DisplayName("ForEach calls the action each element in an iterable")
    default void testForeEachOverCollectionWithElements() {

        CollectionProvider<E, I> provider = provider();
        Iterable<E> iterable = provider.createInstanceWithUniqueElements();
        AssertConsumer<E> action = AssertActions.consumeExactly(iterable);
        iterable.forEach(action);
        action.assertCheck();
    }

    /// Tests that the [Iterable#forEach] throws the correct exception for a `null` action.
    @Test
    @DisplayName("ForEach throws a NullPointerException when called with a null action")
    default void testForEachWithNullAction() {
        Iterable<E> iterable = provider().createInstanceWithUniqueElements();
        Consumer<E> action = null;
        assertThrows(NullPointerException.class, () -> iterable.forEach(action));
    }
}
