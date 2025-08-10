package org.soliscode.test.breakable;

import org.jetbrains.annotations.NotNull;
import org.soliscode.test.contract.CollectionMethods;

import java.util.*;

/// A sequenced collection that can be broken in well-defined ways in order to test collection utilities or testing
/// classes.
///
/// # Breaks
/// The breaks that are supported for this class are listed in the description of the method that they impact.
/// In addition it supports the breaks in the [BreakableIterable], [BreakableIterator], [BreakableSpliterator], and
/// [BreakableCollection] classes. Any iterator or spliterator breaks that are added to this collection will be passes
/// along to iterator or spliterator instances that are created.
///
/// Any breaks that are not listed in the supported classes can be added to an instance of `BreakableSequencedCollection`,
/// but will not have any impact on how it functions.
///
/// Builder methods are provided to make declaring a broken sequenced collection easier, for example:
/// ```java
///     BreakableSequencedCollection<Integer> broken = Breakables.buildSequencedCollection(1, 2)
///         .withBreak(ADD_FIRST_ADDS_TO_END)
///         .build();
/// ```
/// @author evanbergstrom
/// @param <E> The elements type for the `SequencedCollection`.
/// @see CollectionMethods
/// @since 1.0.0
public class BreakableSequencedCollection<E> extends BreakableCollection<E> implements SequencedCollection<E> {

    private final @NotNull List<E> sequenced;

    /// The [addFirst][SequencedCollection#addFirst] method does not add an element.
    /// @see BreakableSequencedCollection#addFirst(Object)
    public static final Break ADD_FIRST_DOES_NOT_ADD_ELEMENT = new Break("addFirst does not add element");

    /// The [addFirst][SequencedCollection#addFirst] method adds the element to the end of the collection.
    /// @see BreakableSequencedCollection#addFirst(Object)
    public static final Break ADD_FIRST_ADDS_TO_END = new Break("addFirst adds to the end");

    /// The [addLast][SequencedCollection#addLast] method does not add an element.
    /// @see BreakableSequencedCollection#addLast(Object)
    public static final Break ADD_LAST_DOES_NOT_ADD_ELEMENT = new Break("addLast does not add element");

    /// The [addLast][SequencedCollection#addLast] method adds the element to the front of the collection.
    /// @see BreakableSequencedCollection#addLast(Object)
    public static final Break ADD_LAST_ADDS_TO_FRONT = new Break("addLast adds to the beginning");

    /// The `getFirst` method returns a `null` value.
    /// @see BreakableSequencedCollection#getFirst()
    public static final Break GET_FIRST_RETURNS_NULL = new Break("getFirst always returns null");

    /// The `getFirst` method always throws a `NoSuchElementException`
    /// @see BreakableSequencedCollection#getFirst()
    public static final Break GET_FIRST_ALWAYS_THROWS = new Break("getFirst always throws");

    /// The `getFirst` method returns the second element.
    /// @see BreakableSequencedCollection#getFirst()
    public static final Break GET_FIRST_SKIPS_FIRST_ELEMENT = new Break("getFirst returns second element");

    /// The `getLast` method returns a `null` value.
    /// @see BreakableSequencedCollection#getLast()
    public static final Break GET_LAST_RETURNS_NULL = new Break("getLast always returns null");

    /// The `getLast` method always throws a `NoSuchElementException`
    /// @see BreakableSequencedCollection#getLast()
    public static final Break GET_LAST_ALWAYS_THROWS = new Break("getLast always throws");

    /// The `getLast` method returns the second element.
    /// @see BreakableSequencedCollection#getLast()
    public static final Break GET_LAST_SKIPS_LAST_ELEMENT = new Break("");

    /// The [removeFirst][SequencedCollection#removeFirst()] method does not remove the first element.
    /// @see BreakableSequencedCollection#removeFirst()
    public static final Break REMOVE_FIRST_DOES_NOT_REMOVE_ELEMENT = new Break("removeFirst() does not remove element");

    /// The [removeFirst][SequencedCollection#removeFirst()] method returns `null`
    /// @see BreakableSequencedCollection#removeFirst()
    public static final Break REMOVE_FIRST_RETURNS_NULL = new Break("removeFirst() always returns null");

    /// The [removeFirst][SequencedCollection#removeFirst()] method always throws a `NoSuchElement` exception.
    /// @see BreakableSequencedCollection#removeFirst()
    public static final Break REMOVE_FIRST_ALWAYS_THROWS = new Break("removeFirst() always throws");

    /// The [removeLast][SequencedCollection#removeLast()] method does not remove the last element.
    /// @see BreakableSequencedCollection#removeLast()
    public static final Break REMOVE_LAST_DOES_NOT_REMOVE_ELEMENT = new Break("removeLast() does not remove element");

    /// The [removeLast][SequencedCollection#removeLast()] method returns `null`
    /// @see BreakableSequencedCollection#removeLast()
    public static final Break REMOVE_LAST_RETURNS_NULL = new Break("removeLast() always returns null");

    /// The [removeLast][SequencedCollection#removeLast()] method always throws a `NoSuchElement` exception.
    /// @see BreakableSequencedCollection#removeLast()
    public static final Break REMOVE_LAST_ALWAYS_THROWS = new Break("removeLast always throws");

    /// The [reversed][java.util.SequencedCollection#reversed] method does not reverse the collection.
    /// @see BreakableSequencedCollection#reversed()
    public static final Break REVERSED_DOES_NOT_REVERSE_COLLECTION = new Break("reversed() does not reverse elements");

    /// The [reversed][java.util.SequencedCollection#reversed] method modifies the collection.
    /// @see BreakableSequencedCollection#reversed()
    public static final Break REVERSED_MODIFIES_THE_COLLECTION = new Break("reversed() modifies the collection");

    /// Creates an empty sequenced collection that has no breaks.
    public BreakableSequencedCollection() {
        this(new ArrayList<>(), new HashSet<>(), 0);
    }

    /// Creates a breakable sequenced collection from an existing instance.
    /// @param other the breakable collection to copy.
    public BreakableSequencedCollection(final @NotNull BreakableSequencedCollection<E> other) {
        this(other.sequenced, new HashSet<>(), 0);
    }

    /// Creates a breakable iterable from an iterable.
    /// @param collection the iterator to use for the elements.
    public BreakableSequencedCollection(final @NotNull List<E> collection) {
        this(collection, new HashSet<>(), 0);
    }

    /// Creates a `BreakableSequencedCollection` from en existing collection and specifying the breaks and collection
    /// characteristics. Rather than calling this constructor directly, consider using the builder
    /// [BreakableCollection.Builder].
    /// @param c               the initial elements for the breakable collection.
    /// @param breaks          the breaks for the collection.
    /// @param characteristics the characteristics for the collection.
    /// @throws NullPointerException if either the `c` or the `breaks` parameters are null.
    public BreakableSequencedCollection(@NotNull List<E> c, @NotNull Collection<Break> breaks, int characteristics) {
        super(c, breaks, characteristics);
        this.sequenced = Objects.requireNonNull(c);
    }

    /// Implements the [reversed][SequencedCollection#reversed] method from the [SequencedCollection] interface. This
    /// method can be broken using the following collection breaks:
    /// - REVERSED_DOES_NOT_REVERSE_COLLECTION
    /// - REVERSED_MODIFIES_THE_COLLECTION
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildSequencedCollection(1,2,3,4,5)
    ///         .withBreak(CollectionBreaks.REVERSED_DOES_NOT_REVERSE_COLLECTION)
    ///         .build();
    /// ```
    /// @return a collection with the elements in the reverse order.
    /// @see SequencedCollection#reversed()
    @Override
    public SequencedCollection<E> reversed() {
        if (supportsMethod(CollectionMethods.Reversed)) {
            if (hasBreak(REVERSED_DOES_NOT_REVERSE_COLLECTION)) {
                return sequenced;
            } else if (hasBreak(REVERSED_MODIFIES_THE_COLLECTION)) {
                Collections.reverse(sequenced);
                return sequenced;
            } else {
                return sequenced.reversed();
            }
        }  else {
            throw new UnsupportedOperationException();
        }
    }

    /// Implements the [addFirst][SequencedCollection#addFirst] method from the [SequencedCollection] interface. This
    /// method can be broken using the following collection breaks:
    /// - ADD_FIRST_DOES_NOT_ADD_ELEMENT
    /// - ADD_FIRST_ADDS_TO_END
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildSequencedCollection(1,2,3,4,5)
    ///         .withBreak(CollectionBreaks.ADD_FIRST_DOES_NOT_ADD_ELEMENT)
    ///         .build();
    /// ```
    /// @throws UnsupportedOperationException if this method is not supported
    /// @throws NullPointerException if the element is null and this collection does not permit null elements.
    /// @see SequencedCollection#addFirst
    @Override
    public void addFirst(E e) {
        if (supportsMethod(CollectionMethods.AddFirst)) {
            checkNewElement(e);
            if(hasBreak(ADD_FIRST_ADDS_TO_END)) {
                sequenced.addLast(e);
            } else if (!hasBreak(ADD_FIRST_DOES_NOT_ADD_ELEMENT)) {
                sequenced.addFirst(e);
            }
        }  else {
            throw new UnsupportedOperationException();
        }
    }

    /// Implements the [addLast][SequencedCollection#addLast] method from the [SequencedCollection] interface. This
    /// method can be broken using the following collection breaks:
    /// - ADD_LAST_DOES_NOT_ADD_ELEMENT
    /// - ADD_LAST_ADDS_TO_FRONT
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildSequencedCollection(1,2,3,4,5)
    ///         .withBreak(CollectionBreaks.ADD_LAST_DOES_NOT_ADD_ELEMENT)
    ///         .build();
    /// ```
    /// @throws UnsupportedOperationException if this method is not supported
    /// @throws NullPointerException if the element is null and this collection does not permit null elements.
    /// @see SequencedCollection#addLast
    @Override
    public void addLast(E e) {
        if (supportsMethod(CollectionMethods.AddLast)) {
            checkNewElement(e);
            if(hasBreak(ADD_LAST_ADDS_TO_FRONT)) {
                sequenced.addFirst(e);
            } else if (!hasBreak(ADD_LAST_DOES_NOT_ADD_ELEMENT)) {
                sequenced.addLast(e);
            }
        }  else {
            throw new UnsupportedOperationException();
        }
    }

    /// Implements the [getFirst][SequencedCollection#getFirst] method from the [SequencedCollection] interface. This
    /// method can be broken using the following collection breaks:
    /// - GET_FIRST_RETURNS_NULL
    /// - GET_FIRST_ALWAYS_THROWS
    /// - GET_FIRST_SKIPS_FIRST_ELEMENT
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildSequencedCollection(1,2,3,4,5)
    ///         .withBreak(CollectionBreaks.GET_FIRST_RETURNS_NULL)
    ///         .build();
    /// ```
    /// @throws UnsupportedOperationException if this method is not supported
    /// @throws NoSuchElementException if this collection is empty
    /// @see SequencedCollection#getFirst
    @Override
    public E getFirst() {
        if (supportsMethod(CollectionMethods.GetFirst)) {
            if (hasBreak(GET_FIRST_RETURNS_NULL)) {
                return null;
            } else if (hasBreak(GET_FIRST_ALWAYS_THROWS)) {
                throw new NoSuchElementException();
            } else if (hasBreak(GET_FIRST_SKIPS_FIRST_ELEMENT)) {
                Iterator<E> i = iterator();
                i.next();
                return i.next();
            } else {
                return sequenced.getFirst();
            }
        }  else {
            throw new UnsupportedOperationException();
        }
    }

    /// Implements the [getLast][SequencedCollection#getLast] method from the [SequencedCollection] interface. This
    /// method can be broken using the following collection breaks:
    /// - GET_LAST_RETURNS_NULL
    /// - GET_LAST_ALWAYS_THROWS
    /// - GET_LAST_SKIPS_FIRST_ELEMENT
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildSequencedCollection(1,2,3,4,5)
    ///         .withBreak(CollectionBreaks.GET_LAST_RETURNS_NULL)
    ///         .build();
    /// ```
    /// @throws UnsupportedOperationException if this method is not supported
    /// @see SequencedCollection#getLast
    @Override
    public E getLast() {
        if (hasBreak(GET_LAST_RETURNS_NULL)) {
            return null;
        } else if (hasBreak(GET_LAST_ALWAYS_THROWS)) {
            throw new NoSuchElementException();
        } else if (hasBreak(GET_LAST_SKIPS_LAST_ELEMENT)) {
            Iterator<E> i = reversed().iterator();
            i.next();
            return i.next();
        } else {
            return sequenced.getLast();
        }
    }

    /// Implements the [removeFirst][SequencedCollection#removeFirst] method from the [SequencedCollection] interface. This
    /// method can be broken using the following collection breaks:
    /// - REMOVE_FIRST_DOES_NOT_REMOVE_ELEMENT
    /// - REMOVE_FIRST_RETURNS_NULL
    /// - REMOVE_FIRST_ALWAYS_THROWS
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildSequencedCollection(1,2,3,4,5)
    ///         .withBreak(CollectionBreaks.REMOVE_FIRST_RETURNS_NULL)
    ///         .build();
    /// ```
    /// @throws UnsupportedOperationException if this method is not supported
    /// @see SequencedCollection#removeFirst
    @Override
    public E removeFirst() {
        if (supportsMethod(CollectionMethods.RemoveFirst)) {
            if (hasBreak(REMOVE_FIRST_DOES_NOT_REMOVE_ELEMENT)) {
                return getFirst();
            } else if (hasBreak(REMOVE_FIRST_RETURNS_NULL)) {
                return null;
            } else if (hasBreak(REMOVE_FIRST_ALWAYS_THROWS)) {
                throw new NoSuchElementException();
            } else {
                return sequenced.removeFirst();
            }
        }  else {
            throw new UnsupportedOperationException();
        }
    }

    /// Implements the [removeLast][SequencedCollection#removeLast] method from the [SequencedCollection] interface. This
    /// method can be broken using the following collection breaks:
    /// - REMOVE_LAST_DOES_NOT_REMOVE_ELEMENT
    /// - REMOVE_LAST_RETURNS_NULL
    /// - REMOVE_LAST_ALWAYS_THROWS
    ///
    /// A collection that has any of these breaks can be constructed using the builder:
    /// ```java
    ///     Collection<Integer> collection = Breakables.buildSequencedCollection(1,2,3,4,5)
    ///         .withBreak(CollectionBreaks.REMOVE_LAST_RETURNS_NULL)
    ///         .build();
    /// ```
    /// @throws UnsupportedOperationException if this method is not supported
    /// @see SequencedCollection#removeLast
    @Override
    public E removeLast() {
        if (supportsMethod(CollectionMethods.RemoveLast)) {
            if (hasBreak(REMOVE_LAST_DOES_NOT_REMOVE_ELEMENT)) {
                return getLast();
            } else if (hasBreak(REMOVE_LAST_RETURNS_NULL)) {
                return null;
            } else if (hasBreak(REMOVE_LAST_ALWAYS_THROWS)) {
                throw new NoSuchElementException();
            } else {
                return sequenced.removeLast();
            }
        }  else {
            throw new UnsupportedOperationException();
        }
    }

    /// The builder for BreakableCollection objects.
    /// @param <E> the element type.
    public static class Builder<E>
            extends AbstractBuilder<BreakableSequencedCollection.Builder<E>, BreakableSequencedCollection<E>, E> {

        private final List<E> list;

        /// Create a builder initialized with the default values.
        public Builder() {
            // super(this.list = new ArrayList<>()); <-- This will work once Flexible Constructors are available
            super(new ArrayList<>());
            this.list = (ArrayList<E>)elements;
        }

        /// Create a builder initialized with an element store.
        /// @param elements the element store to use.
        public Builder(final @NotNull List<E> elements) {
            // super(this.list = Objects.requireNonNull(elements));  <-- This will work once Flexible Constructors are available
            super(Objects.requireNonNull(elements));
            this.list = elements;
        }

        /// Create a builder initialized with the values copied from another builder.
        /// @param other the builder to copy the values from.
        public Builder(final BreakableSequencedCollection.Builder<E> other) {
            super(other);
            this.list = other.list;
        }

        @Override
        public Builder<E> self() {
            return this;
        }

        @Override
        public BreakableSequencedCollection.Builder<E> copy() {
            return new BreakableSequencedCollection.Builder<>(this);
        }

        /// Build a BreakableCollection objects using the values from the builder.
        /// @return a new BreakableCollection object.
        public BreakableSequencedCollection<E> build() {
            BreakableSequencedCollection<E> broken = new BreakableSequencedCollection<>(list, breaks, characteristics);
            broken.setPermitsNulls(permitsNulls);
            broken.setPermitsDuplicates(permitsDuplicates);
            broken.setPermitsIncompatibleTypes(permitsIncompatibleTypes);
            unsupportedMethods.forEach(broken::doesNotSupportMethod);
            return broken;
        }
    }

}
