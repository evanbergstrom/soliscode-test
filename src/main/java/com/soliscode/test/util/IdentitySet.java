package com.soliscode.test.util;


import java.util.AbstractSet;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;

/// An Set that checks for elements by identity rather han by equality. This class is used to implement
/// assertions that are checking for element identity.
/// @param <E> the element type.
/// @author evanbergstrom
/// @since 1.0
public class IdentitySet<E> extends AbstractSet<E> {

    private final IdentityHashMap<E, Boolean> elements;

    /// Creates an empty identity set.
    public IdentitySet() {
        this.elements = new IdentityHashMap<>();
    }

    /// Creates a copy of another identity set.
    /// @param c the identity set to copy.
    public IdentitySet(final Collection<? extends E> c) {
        this.elements = new IdentityHashMap<>();
        c.forEach((e) -> elements.put(e, Boolean.TRUE));
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public Iterator<E> iterator() {
        return elements.keySet().iterator();
    }

    @Override
    public boolean contains(final Object o) {
        return elements.containsKey(o);
    }

    @Override
    public boolean add(final E e) {
        return elements.put(e, Boolean.TRUE) == null;
    }

    @Override
    public boolean remove(final Object o) {
        return elements.remove(o) != null;
    }
}
