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

package org.soliscode.test.assertions.collection;

import org.junit.jupiter.api.Assertions;
import org.soliscode.test.interfaces.IterableOnly;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

/// A utility class for assertion classes that assert conditions on iterables.
///
/// @author evanbergstrom
/// @since 1.0.0
public class IterableAssertion {

    protected IterableAssertion() {
    }

    protected static void assertIterablesNotNull(final Object actual, final Object messageOrSupplier) {

        if (actual == null) {
            failIterableIsNull("actual", messageOrSupplier);
        }
    }

    protected static void assertIterablesNotNull(final Object expected, final Object actual,
        final Object messageOrSupplier) {

        if (expected == null) {
            failIterableIsNull("expected", messageOrSupplier);
        }
        if (actual == null) {
            failIterableIsNull("actual", messageOrSupplier);
        }
    }

    protected static void failIterableIsNull(final String name, final Object messageOrSupplier) {
        assertionFailure()
            .message(messageOrSupplier)
            .reason(name + " iterable was <null>")
            .buildAndThrow();
    }

    protected static <E> Iterable<E> asIterable(E[] elements) {
        Assertions.assertNotNull(elements);
        return new IterableOnly<>(Arrays.stream(elements).collect(Collectors.toList()));
    }
}
