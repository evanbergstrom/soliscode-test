package org.soliscode.test.breakable;

import org.jspecify.annotations.NonNull;

public interface BreakableCollectionBuilder<C extends Breakable, E>
        extends BreakableBuilder<C> {

    C build(final @NonNull Iterable<E> i);
}
