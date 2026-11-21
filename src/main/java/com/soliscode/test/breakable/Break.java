/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.breakable;


import java.io.Serializable;

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
/// // Create a list whose forEach skips the first element
/// List<String> brokenList = Breakables.buildList("a", "b", "c")
///     .addBreak(BreakableList.FOR_EACH_SKIPS_FIRST_ELEMENT)
///     .build();
///
/// // forEach sees only "b" and "c", not "a"
/// List<String> seen = new ArrayList<>();
/// brokenList.forEach(seen::add);
/// assertEquals(List.of("b", "c"), seen);
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
public record Break(String description) implements Serializable {

    /// Returns the name that identifies the break in messages: its description.
    ///
    /// @return the break's description
    public String name() {
        return description;
    }
}
