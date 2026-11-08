package com.soliscode.test.provider;


import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

/// An objectProvider that uses a set of functions to construct instances of the class. There are two
/// elements required to create a collection objectProvider using this class:
/// - Default constructor function
/// - Copy constructor function
///
/// For example, to create a objectProvider for Integer objects:
/// ```java
///     Provider<Integer> objectProvider = FunctionalProvider<Integer>(Integer;:new, Integer::new);
/// ```
///
/// @param <T> the provided type.
/// @author evanbergstrom
/// @since 1.0
public class FunctionalProvider<T> implements ObjectProvider<T> {

    private final Supplier<T> defaultConstructor;
    private final Function<T, T> copyConstructor;
    private final @Nullable Function<Long, T> seedConstructor;

    /// Create an instance of this collection objectProvider that uses the methods and element objectProvider specified
    /// in the arguments for its implementation.
    /// @param defaultConstructorArg the supplier to use to create default instances of the collection.
    /// @param copyConstructorArg the function to use to create a copy of the collection.
    /// @param seedConstructorArg the function to use to create an instance of the class from an integer seed value.
    /// @throws NullPointerException if any of the arguments are `null`
    public FunctionalProvider(final Supplier<T> defaultConstructorArg,
                              final Function<T, T> copyConstructorArg,
                              final Function<Long, T> seedConstructorArg) {
        this.defaultConstructor = Objects.requireNonNull(defaultConstructorArg);
        this.copyConstructor = Objects.requireNonNull(copyConstructorArg);
        this.seedConstructor = Objects.requireNonNull(seedConstructorArg);
    }

    /// Create an instance of this collection objectProvider that uses the methods and element objectProvider specified
    /// in the arguments for its implementation.
    /// @param defaultConstructorArg the supplier to use to create default instances of the collection.
    /// @param copyConstructorArg the function to use to create a copy of the collection.
    /// @throws NullPointerException if any of the arguments are `null`
    protected FunctionalProvider(final Supplier<T> defaultConstructorArg,
                                 final Function<T, T> copyConstructorArg) {
        this.defaultConstructor = Objects.requireNonNull(defaultConstructorArg);
        this.copyConstructor = Objects.requireNonNull(copyConstructorArg);
        this.seedConstructor = null;
    }

    @Override
    public T defaultInstance() {
        return defaultConstructor.get();
    }

    @Override
    public T copyInstance(final T o) {
        return copyConstructor.apply(o);
    }

    @Override
    public T createInstance(final long seed) {
        if (seedConstructor != null) {
            return seedConstructor.apply(seed);
        } else {
            throw new IllegalStateException("subclasses must override the createInstance(seed) method");
        }
    }
}
