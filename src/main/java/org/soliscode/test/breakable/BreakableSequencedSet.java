package org.soliscode.test.breakable;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.MethodStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SequencedSet;
import java.util.Set;

/// **Breakable SequencedSet Implementation for Testing**
///
/// This class extends `BreakableSet` to provide a `SequencedSet` implementation that can be
/// programmatically broken for comprehensive testing scenarios. It maintains set semantics
/// (no duplicate elements) while adding sequence-specific operations (`addFirst`, `addLast`,
/// `getFirst`, `getLast`, `removeFirst`, `removeLast`) that can be individually configured to fail
/// in various ways.
///
/// ## Core Functionality
///
/// As a `SequencedSet` implementation, this class supports all standard set operations plus
/// sequence-specific operations that provide ordered access to elements. The class can be
/// configured to break these sequence-specific contracts through targeted breaks while
/// maintaining compatibility with the broader set testing framework.
///
/// ### SequencedSet Semantics
///
/// Under normal operation (no breaks active), `BreakableSequencedSet` maintains proper `SequencedSet` behavior:
/// - **Ordered Access**: `getFirst()` and `getLast()` return the first and last elements respectively.
/// - **Ordered Insertion**: `addFirst()` and `addLast()` insert elements at specific positions.
/// - **Ordered Removal**: `removeFirst()` and `removeLast()` remove and return specific elements.
/// - **Sequence Reversal**: `reversed()` returns a view with elements in reverse order.
/// - **Exception Handling**: Proper `NoSuchElementException` throwing for empty sets.
///
/// ### Breakable Behavior
///
/// This class utilizes breaks from `BreakableSequencedCollection` to simulate various failures
/// in sequenced operations, such as:
/// - Positional addition failures (e.g., `ADD_FIRST_DOES_NOT_ADD_ELEMENT`)
/// - Positional access failures (e.g., `GET_FIRST_ALWAYS_THROWS`)
/// - Positional removal failures (e.g., `REMOVE_FIRST_RETURNS_NULL`)
/// - Reversal failures (e.g., `REVERSED_DOES_NOT_REVERSE_COLLECTION`)
///
/// ## Usage Examples
///
/// ### Testing Positional Insertion Issues
/// ```java
/// // Create a set where addFirst adds to the end instead
/// SequencedSet<String> brokenSet = new BreakableSequencedSet.Builder<String>()
///     .withBreak(BreakableSequencedCollection.ADD_FIRST_ADDS_TO_END)
///     .build();
///
/// brokenSet.add_singleElement_returnsTrueAndUpdatesSize("middle");
/// brokenSet.addFirst("first");
/// assertEquals("first", new ArrayList<>(brokenSet).get(1)); // "first" is at the end
/// ```
///
/// ### Testing Reversal Failures
/// ```java
/// // Create a set where reversed() returns the original set
/// SequencedSet<Integer> brokenSet = new BreakableSequencedSet.Builder<Integer>()
///     .add_singleElement_returnsTrueAndUpdatesSize(1).add_singleElement_returnsTrueAndUpdatesSize(2)
///     .withBreak(BreakableSequencedCollection.REVERSED_DOES_NOT_REVERSE_COLLECTION)
///     .build();
///
/// assertEquals(1, brokenSet.reversed().getFirst()); // Still 1
/// ```
///
/// ## Thread Safety
///
/// This class is not thread-safe. External synchronization is required for concurrent access.
///
/// @param <E> the type of elements maintained by this sequenced set
/// @author evanbergstrom
/// @since 1.0.0
/// @see BreakableSet
/// @see SequencedSet
/// @see BreakableSequencedCollection
/// @see java.util.LinkedHashSet
public class BreakableSequencedSet<E> extends BreakableSet<E> implements SequencedSet<E> {

    private final SequencedSet<E> sequencedSet;

    /// Creates a new empty `BreakableSequencedSet` with default configuration.
    ///
    /// The set uses a `LinkedHashSet` as the backing collection and has no breaks active.
    public BreakableSequencedSet() {
        this(new LinkedHashSet<>(), new HashSet<>(), new HashMap<>(), DEFAULT_CHARACTERISTICS, DEFAULT_PERMITS,
                DEFAULT_SAFETY, Object.class);
    }

    /// Creates a new `BreakableSequencedSet` as a shallow copy of another `BreakableSequencedSet`.
    ///
    /// @param other the `BreakableSequencedSet` to copy from
    /// @throws NullPointerException if `other` is null
    public BreakableSequencedSet(final @NonNull BreakableSequencedSet<E> other) {
        this(new LinkedHashSet<>(other.sequencedSet), new HashSet<>(other.breaks()),
                new HashMap<>(other.methodStatuses()), other.characteristics(), other.permits(), other.isSafe(),
                other.compatibleType());
    }

    /// Creates a new `BreakableSequencedSet` using the specified sequenced set as the backing store.
    ///
    /// @param sequencedSet the backing sequenced set to use for element storage
    /// @throws NullPointerException if `sequencedSet` is null
    public BreakableSequencedSet(final @NonNull SequencedSet<E> sequencedSet) {
        this(sequencedSet, new HashSet<>(), new HashMap<>(), DEFAULT_CHARACTERISTICS, DEFAULT_PERMITS, DEFAULT_SAFETY,
                Object.class);
    }

    /// Creates a new `BreakableSequencedSet` with full configuration control.
    ///
    /// @param sequencedSet the backing sequenced set for element storage
    /// @param breaks the collection of breaks to activate
    /// @param methodStatuses the method status configuration
    /// @param characteristics the spliterator characteristics
    /// @param permits the flags that indicate what types of values are supported by the collection
    /// @throws NullPointerException if `sequencedSet`, `breaks`, or `methodStatuses` is null
    protected BreakableSequencedSet(final @NonNull SequencedSet<E> sequencedSet, final @NonNull Set<Break> breaks,
                                    final @NonNull Map<InterfaceMethod, MethodStatus> methodStatuses,
                                    final int characteristics, final int permits, final boolean isSafe,
                                    final Class<?> compatibleType) {
        super(sequencedSet, breaks, methodStatuses, characteristics, permits, isSafe, compatibleType);
        this.sequencedSet = sequencedSet;
    }

    /// Returns a reverse-ordered view of this set.
    ///
    /// This method can be broken using:
    /// - `REVERSED_DOES_NOT_REVERSE_COLLECTION`: returns the original set.
    /// - `REVERSED_MODIFIES_THE_COLLECTION`: actually reverses the backing collection.
    ///
    /// @return a reverse-ordered view of this set
    @Override
    public SequencedSet<E> reversed() {
        if (hasBreak(BreakableSequencedCollection.REVERSED_DOES_NOT_REVERSE_COLLECTION)) {
            return this;
        }
        if (hasBreak(BreakableSequencedCollection.REVERSED_MODIFIES_THE_COLLECTION)) {
            SequencedSet<E> reversed = sequencedSet.reversed();
            sequencedSet.clear();
            sequencedSet.addAll(reversed);
            return this;
        }
        return new BreakableSequencedSet<>(sequencedSet.reversed(), breaks(), methodStatuses(), characteristics(),
                permits(), isSafe(), compatibleType());
    }

    /// Adds an element as the first element of this set.
    ///
    /// This method can be broken using:
    /// - `ADD_FIRST_DOES_NOT_ADD_ELEMENT`: method completes but element is not added.
    /// - `ADD_FIRST_ADDS_TO_END`: element is added to the end instead.
    ///
    /// @param e the element to add_singleElement_returnsTrueAndUpdatesSize
    @Override
    public void addFirst(final E e) {
        checkArgument(e);
        if (hasBreak(BreakableSequencedCollection.ADD_FIRST_DOES_NOT_ADD_ELEMENT)) {
            return;
        }
        if (hasBreak(BreakableSequencedCollection.ADD_FIRST_ADDS_TO_END)) {
            addLast(e);
            return;
        }
        sequencedSet.addFirst(e);
    }

    /// Adds an element as the last element of this set.
    ///
    /// This method can be broken using:
    /// - `ADD_LAST_DOES_NOT_ADD_ELEMENT`: method completes but element is not added.
    /// - `ADD_LAST_ADDS_TO_FRONT`: element is added to the front instead.
    ///
    /// @param e the element to add_singleElement_returnsTrueAndUpdatesSize
    @Override
    public void addLast(final E e) {
        checkArgument(e);
        if (hasBreak(BreakableSequencedCollection.ADD_LAST_DOES_NOT_ADD_ELEMENT)) {
            return;
        }
        if (hasBreak(BreakableSequencedCollection.ADD_LAST_ADDS_TO_FRONT)) {
            addFirst(e);
            return;
        }
        sequencedSet.addLast(e);
    }

    /// Gets the first element of this set.
    ///
    /// This method can be broken using:
    /// - `GET_FIRST_ALWAYS_THROWS`: always throws `NoSuchElementException`.
    /// - `GET_FIRST_RETURNS_NULL`: returns `null` even if elements exist.
    /// - `GET_FIRST_SKIPS_FIRST_ELEMENT`: returns the second element instead.
    ///
    /// @return the first element
    /// @throws NoSuchElementException if this set is empty (or if broken)
    @Override
    public E getFirst() {
        if (hasBreak(BreakableSequencedCollection.GET_FIRST_ALWAYS_THROWS)) {
            throw new NoSuchElementException();
        }
        if (sequencedSet.isEmpty()) {
            throw new NoSuchElementException();
        }
        if (hasBreak(BreakableSequencedCollection.GET_FIRST_RETURNS_NULL)) {
            return null;
        }
        if (hasBreak(BreakableSequencedCollection.GET_FIRST_SKIPS_FIRST_ELEMENT)) {
            if (sequencedSet.size() < 2) {
                throw new NoSuchElementException();
            }
            return new ArrayList<>(sequencedSet).get(1);
        }
        return sequencedSet.getFirst();
    }

    /// Gets the last element of this set.
    ///
    /// This method can be broken using:
    /// - `GET_LAST_ALWAYS_THROWS`: always throws `NoSuchElementException`.
    /// - `GET_LAST_RETURNS_NULL`: returns `null` even if elements exist.
    /// - `GET_LAST_SKIPS_LAST_ELEMENT`: returns the second-to-last element instead.
    ///
    /// @return the last element
    /// @throws NoSuchElementException if this set is empty (or if broken)
    @Override
    public E getLast() {
        if (hasBreak(BreakableSequencedCollection.GET_LAST_ALWAYS_THROWS)) {
            throw new NoSuchElementException();
        }
        if (sequencedSet.isEmpty()) {
            throw new NoSuchElementException();
        }
        if (hasBreak(BreakableSequencedCollection.GET_LAST_RETURNS_NULL)) {
            return null;
        }
        if (hasBreak(BreakableSequencedCollection.GET_LAST_SKIPS_LAST_ELEMENT)) {
            if (sequencedSet.size() < 2) {
                throw new NoSuchElementException();
            }
            return new ArrayList<>(sequencedSet).get(sequencedSet.size() - 2);
        }
        return sequencedSet.getLast();
    }

    /// Removes and returns the first element of this set.
    ///
    /// This method can be broken using:
    /// - `REMOVE_FIRST_ALWAYS_THROWS`: always throws `NoSuchElementException`.
    /// - `REMOVE_FIRST_RETURNS_NULL`: removes the element but returns `null`.
    /// - `REMOVE_FIRST_DOES_NOT_REMOVE_ELEMENT`: returns the first element but doesn't remove it.
    ///
    /// @return the removed element
    /// @throws NoSuchElementException if this set is empty (or if broken)
    @Override
    public E removeFirst() {
        if (hasBreak(BreakableSequencedCollection.REMOVE_FIRST_ALWAYS_THROWS)) {
            throw new NoSuchElementException();
        }
        if (sequencedSet.isEmpty()) {
            throw new NoSuchElementException();
        }
        if (hasBreak(BreakableSequencedCollection.REMOVE_FIRST_RETURNS_NULL)) {
            sequencedSet.removeFirst();
            return null;
        }
        if (hasBreak(BreakableSequencedCollection.REMOVE_FIRST_DOES_NOT_REMOVE_ELEMENT)) {
            return sequencedSet.getFirst();
        }
        return sequencedSet.removeFirst();
    }

    /// Removes and returns the last element of this set.
    ///
    /// This method can be broken using:
    /// - `REMOVE_LAST_ALWAYS_THROWS`: always throws `NoSuchElementException`.
    /// - `REMOVE_LAST_RETURNS_NULL`: removes the element but returns `null`.
    /// - `REMOVE_LAST_DOES_NOT_REMOVE_ELEMENT`: returns the last element but doesn't remove it.
    ///
    /// @return the removed element
    /// @throws NoSuchElementException if this set is empty (or if broken)
    @Override
    public E removeLast() {
        if (hasBreak(BreakableSequencedCollection.REMOVE_LAST_ALWAYS_THROWS)) {
            throw new NoSuchElementException();
        }
        if (sequencedSet.isEmpty()) {
            throw new NoSuchElementException();
        }
        if (hasBreak(BreakableSequencedCollection.REMOVE_LAST_RETURNS_NULL)) {
            sequencedSet.removeLast();
            return null;
        }
        if (hasBreak(BreakableSequencedCollection.REMOVE_LAST_DOES_NOT_REMOVE_ELEMENT)) {
            return sequencedSet.getLast();
        }
        return sequencedSet.removeLast();
    }

    /// **Builder for creating `BreakableSequencedSet` instances.**
    ///
    /// Provides a fluent API for configuring and constructing `BreakableSequencedSet` instances
    /// with specific backing sets, elements, and breaks.
    ///
    /// @param <E> the element type
    public static class Builder<E> extends AbstractBuilder<Builder<E>, BreakableSequencedSet<E>, E> {

        private final SequencedSet<E> backingSet;

        /// Creates a new `Builder` with an empty `LinkedHashSet` as the backing set.
        public Builder() {
            this(new LinkedHashSet<>());
        }

        /// Creates a new `Builder` using the specified `SequencedSet` as the backing store.
        /// @param sequencedSet the backing set to use
        public Builder(final SequencedSet<E> sequencedSet) {
            super();
            this.backingSet = sequencedSet;
        }

        /// Creates a new `Builder` populated with the specified elements.
        /// @param elements the initial elements
        public Builder(final Collection<E> elements) {
            this(new LinkedHashSet<>(elements));
        }

        /// Creates a new `Builder` by copying configuration from another builder.
        /// @param other the builder to copy from
        public Builder(final Builder<E> other) {
            super(other);
            this.backingSet = new LinkedHashSet<>(other.backingSet);
        }

        /// {@inheritDoc}
        @Override
        public Builder<E> self() {
            return this;
        }

        /// {@inheritDoc}
        @Override
        public Builder<E> copy() {
            return new Builder<>(this);
        }

        /// {@inheritDoc}
        @Override
        public BreakableSequencedSet<E> build() {
            return new BreakableSequencedSet<>(new LinkedHashSet<>(backingSet), breaks(), methodStatuses(),
                    characteristics(), permits(), isSafe(), compatibleType());
        }
    }
}
