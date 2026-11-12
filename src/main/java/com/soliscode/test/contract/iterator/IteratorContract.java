/*
 * Copyright 2026 Evan Bergstrom
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
package com.soliscode.test.contract.iterator;

import java.util.Iterator;

/// **Contract for the [Iterator] interface.**
///
/// A test class implements it with an [com.soliscode.test.provider.IteratorProvider] of the iterator under test and
/// an element provider; JUnit runs its test methods as the class's own. Where the iterator does not support
/// `remove()`, the class says so through `supportsMethod`
/// ([com.soliscode.test.contract.iterable.IterableMethods#ITERATOR_REMOVE]).
///
/// It follows the Javadoc of [Iterator], and where it reads it strictly or loosely, the method contract says so:
/// `forEachRemaining` is not optional ([ForEachRemainingContract]), an unsupported `remove()` is checked only for
/// [UnsupportedOperationException] ([RemoveContract]), and order is checked only where the source has one
/// ([com.soliscode.test.provider.IteratorSource#ordered()]). A test runs the branch for a supported method only if
/// every optional method it calls is supported. It does not test an iterator whose source is changed other than
/// through it, which [Iterator] leaves unspecified; [WeaklyConsistentIteratorContract] tests one that promises to be
/// weakly consistent.
///
/// ## Usage Examples
/// ```java
/// class ArrayListIteratorTest extends AbstractTest
///         implements IteratorContract<Iterator<Integer>, Integer>, WithIntegerElement {
///     @Override
///     public IteratorProvider<Iterator<Integer>, Integer> iteratorProvider() {
///         return IteratorProviders.arrayList();
///     }
/// }
/// ```
///
/// @param <I> the type of the iterator under test
/// @param <E> the element type
/// @since 0.1.3
/// @see Iterator
public interface IteratorContract<I extends Iterator<E>, E>
        extends HasNextContract<I, E>, NextContract<I, E>, RemoveContract<I, E>, ForEachRemainingContract<I, E> {
}
