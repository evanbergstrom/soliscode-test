package org.soliscode.test.provider;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/// An provider that uses a set of functions to construct instances of the class. There are two
/// elements required to create a collection provider using this class:
/// - Default constructor function
/// - Copy constructor function
///
/// For example, to create a provider for Integer objects:
/// ```java
///     Provider<Integer> provider = FunctionalProvider<Integer>(Integer;:new, Integer::new);
/// ```
///
/// @param <T> the provided type.
/// @author evanbergstrom
/// @since 1.0
public class FunctionalProvider<T> implements ObjectProvider<T> {

    private final @NotNull Supplier<T> defaultConstructor;
    private final @NotNull Function<T, T> copyConstructor;
    private final Function<Integer, T> seedConstructor;

    /// Create an instance of this collection provider that uses the methods and element provider specified in the
    /// arguments for its implementation.
    /// @param defaultConstructor the supplier to use to create default instances of the collection.
    /// @param copyConstructor the function to use to create a copy of the collection.
    /// @param seedConstructor the function to use to create an instance of the class from an integer seed value.
    /// @throws NullPointerException if any of the arguments are `null`
    public FunctionalProvider(final @NotNull Supplier<T> defaultConstructor,
                              final @NotNull Function<T, T> copyConstructor,
                              final @NotNull Function<Integer, T> seedConstructor) {
        this.defaultConstructor = Objects.requireNonNull(defaultConstructor);
        this.copyConstructor = Objects.requireNonNull(copyConstructor);
        this.seedConstructor = Objects.requireNonNull(seedConstructor);
    }

    /// Create an instance of this collection provider that uses the methods and element provider specified in the
    /// arguments for its implementation.
    /// @param defaultConstructor the supplier to use to create default instances of the collection.
    /// @param copyConstructor the function to use to create a copy of the collection.
    /// @throws NullPointerException if any of the arguments are `null`
    protected FunctionalProvider(final @NotNull Supplier<T> defaultConstructor,
                                 final @NotNull Function<T, T> copyConstructor) {
        this.defaultConstructor = Objects.requireNonNull(defaultConstructor);
        this.copyConstructor = Objects.requireNonNull(copyConstructor);
        this.seedConstructor = null;
    }

    @Override
    public @NotNull T defaultInstance() {
        return defaultConstructor.get();
    }

    @Override
    public @NotNull T copyInstance(final @NotNull T o) {
        return copyConstructor.apply(o);
    }

    @Override
    public @NotNull T createInstance(int seed) {
        if (seedConstructor != null) {
            return seedConstructor.apply(seed);
        } else {
            throw new IllegalStateException("subclasses must override the createInstance(seed) method");
        }
    }
}
