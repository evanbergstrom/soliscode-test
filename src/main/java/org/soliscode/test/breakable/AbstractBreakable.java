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
import org.soliscode.test.OptionalMethodSupport;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/// Abstract base class that provides common functionality for implementing breakable collection classes.
///
/// This class serves as the foundation for all breakable collection implementations, providing:
/// - Break management (adding, checking, and retrieving breaks)
/// - Optional method support inheritance from {@link OptionalMethodSupport}
/// - Thread-safe break storage and manipulation
/// - Standard constructors for different initialization scenarios
///
/// ## Purpose
///
/// AbstractBreakable eliminates boilerplate code when creating new breakable collection types by providing
/// a complete implementation of the {@link Breakable} interface. Subclasses only need to implement the
/// specific collection interface methods and apply the breaks appropriately.
///
/// ## Usage Pattern
///
/// Typical usage involves extending this class and implementing the desired collection interface:
///
/// ```java
/// public class BreakableQueue<E> extends AbstractBreakable implements Queue<E> {
///     private final Queue<E> delegate;
///
///     public BreakableQueue(Queue<E> delegate, Collection<Break> breaks) {
///         super(breaks);
///         this.delegate = delegate;
///     }
///
///     @Override
///     public boolean offer(E e) {
///         if (hasBreak(OFFER_ALWAYS_RETURNS_FALSE)) {
///             return false;
///         }
///         return delegate.offer(e);
///     }
///
///     // ... other Queue methods with break handling
/// }
/// ```
///
/// ## Break Management
///
/// This class maintains an internal set of {@link Break} instances that define how the collection's
/// behavior should deviate from the standard contract. Breaks can be:
/// - Set during construction
/// - Copied from another breakable object
/// - Added dynamically after construction
/// - Queried to determine if specific behavior modifications should be applied
///
/// ## Thread Safety
///
/// The break management operations in this class are not inherently thread-safe. Subclasses that need
/// thread safety should implement appropriate synchronization or use concurrent collections for the
/// internal break storage.
///
/// ## Inheritance Hierarchy
///
/// This class extends {@link OptionalMethodSupport}, which provides the ability to mark certain
/// collection methods as unsupported, causing them to throw {@link UnsupportedOperationException}.
/// This is useful for testing scenarios where collections have partial interface implementations.
///
/// @author evanbergstrom
/// @since 1.0
/// @see Breakable
/// @see Break
/// @see OptionalMethodSupport
/// @see BreakableIterable
/// @see BreakableList
/// @see BreakableCollection
public abstract class AbstractBreakable extends OptionalMethodSupport implements Breakable {

    private final @NonNull Set<Break> breaks;

    /// Creates a breakable object with no breaks applied.
    ///
    /// This constructor initializes an empty break set, creating a collection that behaves
    /// according to the standard interface contract. Breaks can be added later using
    /// {@link #addBreaks(Collection)}.
    ///
    /// This is typically used when you want to create a baseline collection for testing
    /// or when breaks will be determined and added dynamically.
    public AbstractBreakable() {
        super();
        this.breaks = new HashSet<>();
    }

    /// Creates a breakable object by copying the breaks and optional method support from another breakable object.
    ///
    /// This copy constructor creates a deep copy of the break set and optional method configuration
    /// from the source object. The resulting object will have the same behavioral modifications
    /// but is completely independent of the original.
    ///
    /// This is useful for creating variations of existing breakable objects or for implementing
    /// clone-like functionality in subclasses.
    ///
    /// @param other the breakable object to copy breaks and method support from
    /// @throws NullPointerException if other is null
    public AbstractBreakable(final @NonNull AbstractBreakable other) {
        super(other);
        this.breaks = new HashSet<>(other.breaks);
    }

    /// Creates a breakable object with the specified collection of breaks.
    ///
    /// This constructor initializes the break set with the provided breaks, allowing
    /// immediate application of behavioral modifications. The breaks collection is copied
    /// to prevent external modification of the internal break set.
    ///
    /// This is the most common constructor for creating breakable objects with known
    /// behavioral deviations, typically used in test scenarios.
    ///
    /// @param breaks the collection of breaks to apply to this object; must not be null
    ///               and may be empty to create an unbroken object
    /// @throws NullPointerException if breaks is null
    public AbstractBreakable(final @NonNull Collection<Break> breaks) {
        super();
        this.breaks = new HashSet<>(Objects.requireNonNull(breaks));
    }

    /// {@inheritDoc}
    ///
    /// This implementation performs an efficient set lookup to determine if the specified
    /// break is currently applied to this object. The operation is typically O(1) for
    /// HashSet implementations.
    ///
    /// @param aBreak the break to check for; must not be null
    /// @return true if the break is currently applied, false otherwise
    /// @throws NullPointerException if aBreak is null
    @Override
    public boolean hasBreak(final @NonNull Break aBreak) {
        return breaks.contains(Objects.requireNonNull(aBreak));
    }

    /// {@inheritDoc}
    ///
    /// This implementation returns an unmodifiable view of the internal break set,
    /// preventing external modification while allowing iteration and inspection.
    /// The returned set reflects the current state of breaks and will not change
    /// even if breaks are added to this object later.
    ///
    /// @return an unmodifiable set containing all currently applied breaks;
    ///         never null but may be empty
    @Override
    public @NonNull Set<Break> breaks() {
        return Collections.unmodifiableSet(breaks);
    }

    /// {@inheritDoc}
    ///
    /// This implementation adds a provided breaks to the internal break set.
    /// Duplicate breaks are automatically handled by the set semantics - adding
    /// a break that already exists has no effect.
    ///
    /// This method allows dynamic modification of the object's behavior after
    /// construction, which can be useful for testing scenarios where breaks
    /// need to be applied conditionally.
    ///
    /// @param aBreak the break to add; must not be null
    /// @throws NullPointerException if aBreak is null
    @Override
    public void addBreak(final @NonNull Break aBreak) {
        this.breaks.add(aBreak);
    }

    /// {@inheritDoc}
    ///
    /// This implementation adds all provided breaks to the internal break set.
    /// Duplicate breaks are automatically handled by the set semantics - adding
    /// a break that already exists has no effect.
    ///
    /// This method allows dynamic modification of the object's behavior after
    /// construction, which can be useful for testing scenarios where breaks
    /// need to be applied conditionally.
    ///
    /// @param newBreaks the collection of breaks to add; must not be null
    ///                  but may be empty (which results in no changes)
    /// @throws NullPointerException if newBreaks is null
    @Override
    public void addBreaks(final @NonNull Collection<Break> newBreaks) {
        this.breaks.addAll(newBreaks);
    }
}
