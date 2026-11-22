/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.guide;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/// The list the first steps test, standing in for a collection of the reader's own: an [AbstractList] that keeps
/// its elements in an [ArrayList].
///
/// @param <E> the type of the elements
public class ForwardingList<E> extends AbstractList<E> {

    private final List<E> elements;

    /// Makes an empty list.
    public ForwardingList() {
        this.elements = new ArrayList<>();
    }

    /// Makes a list of the elements given, in their iteration order.
    ///
    /// @param initial the elements
    public ForwardingList(final Collection<? extends E> initial) {
        this.elements = new ArrayList<>(initial);
    }

    @Override
    public E get(final int index) {
        return elements.get(index);
    }

    @Override
    public E set(final int index, final E element) {
        return elements.set(index, element);
    }

    @Override
    public void add(final int index, final E element) {
        elements.add(index, element);
        modCount++;
    }

    @Override
    public E remove(final int index) {
        E removed = elements.remove(index);
        modCount++;
        return removed;
    }

    /// Removes every element at once: [AbstractList]'s own removes them one at a time from the front.
    @Override
    public void clear() {
        modCount++;
        elements.clear();
    }

    @Override
    public int size() {
        return elements.size();
    }
}
