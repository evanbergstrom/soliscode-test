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

package org.soliscode.test.breakable;

import org.jspecify.annotations.NonNull;

/// Represents a specific way to intentionally break the behavior of collection implementations for testing purposes.
///
/// A Break is a named defect or deviation from the standard contract of a collection interface method.
/// Breaks are used with Breakable collection implementations (like {@link BreakableList}, {@link BreakableIterable},
/// etc.) to test that collection testing utilities and contracts properly detect incorrect behavior.
///
/// ## Purpose
///
/// The Break system allows developers to:
/// - Test collection testing frameworks by introducing known defects
/// - Verify that contract tests properly catch specification violations
/// - Simulate real-world implementation bugs in a controlled manner
/// - Validate the robustness of collection utilities and testing code
///
/// ## Usage Example
///
/// ```java
/// // Create a list that skips the first element when iterating
/// List<String> brokenList = Breakables.buildList("a", "b", "c")
///     .addBreak(BreakableList.ITERATOR_SKIPS_FIRST_ELEMENT)
///     .build();
///
/// // This iteration will only see "b" and "c", not "a"
/// brokenList.forEach(System.out::println);
/// ```
///
/// ## Break Categories
///
/// Breaks typically fall into several categories:
/// - **Iterator breaks**: Affect iterator behavior (hasNext, next, remove, forEachRemaining)
/// - **Collection breaks**: Affect collection operations (add, remove, contains, size)
/// - **List breaks**: Affect list-specific operations (get, set, indexOf, subList)
/// - **Spliterator breaks**: Affect spliterator behavior (tryAdvance, trySplit, forEachRemaining)
/// - **Exception breaks**: Cause methods to throw incorrect exceptions
///
/// ## Implementation Notes
///
/// Each Break instance should have a descriptive name that clearly indicates what behavior is modified.
/// Breaks are typically defined as static final constants in the classes that support them.
/// Multiple breaks can be applied to a single collection to test complex failure scenarios.
///
/// @param description A clear, concise description of how this break modifies the normal behavior
///                   of the collection. Should be written in present tense describing the deviation
///                   (e.g., "iterator skips the first element", "add method always returns false").
/// @author Evan Bergstrom
/// @since 1.0
/// @see AbstractBreakable
/// @see BreakableIterable
/// @see BreakableList
/// @see BreakableCollection
public record Break(@NonNull String description) {
}
