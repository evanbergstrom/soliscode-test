package org.soliscode.test.breakable;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.MethodStatus;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/// Base builder class for creating breakable objects with configurable breaks and method support.
///
/// This abstract class provides the core functionality for builders that create `Breakable`
/// instances. it maintains a set of `Break` objects to be applied and a map of `InterfaceMethod`
/// statuses to configure method support.
///
/// ## Overview
///
/// `AbstractBreakableBuilder` is designed to be extended by specific breakable component builders.
/// It provides a fluent API for:
/// - Adding behavioral breaks to the resulting object.
/// - Explicitly disabling support for optional methods.
/// - Creating defensive copies of the builder configuration.
///
/// ## Usage Example
///
/// ```java
/// public class MyBreakableBuilder extends AbstractBreakableBuilder<MyBreakableBuilder, MyBreakable> {
///     @Override
///     public MyBreakableBuilder self() { return this; }
///
///     @Override
///     public MyBreakableBuilder copy() { return new MyBreakableBuilder(this); }
///
///     @Override
///     public MyBreakable build() {
///         return new MyBreakable(breaks(), methodStatuses());
///     }
/// }
///
/// // Usage:
/// MyBreakable breakable = new MyBreakableBuilder()
///     .addBreak(SOME_BREAK)
///     .doesNotSupport(InterfaceMethod.Add)
///     .build();
/// ```
///
/// ## Thread Safety
///
/// This builder is not thread-safe. If multiple threads access a builder instance concurrently,
/// it must be synchronized externally.
///
/// @param <B> the type of the builder itself for method chaining
/// @param <C> the type of the `Breakable` object being built
/// @since 1.0
public abstract class AbstractBreakableBuilder<B extends AbstractBreakableBuilder<B, C>, C extends Breakable> {

    /// The set of breaks that will be applied to the breakable object.
    private final @NonNull Set<Break> breaks;

    /// The set of optional methods that will not be supported by the breakable object.
    private final @NonNull Map<InterfaceMethod, MethodStatus> methodStatuses;

    /// Whether the breakable object is safe for concurrent access.
    private boolean isSafe = false;

    /// Creates a new `AbstractBreakableBuilder` with an empty configuration.
    ///
    /// The resulting builder will have no breaks and all optional methods will be supported
    /// by default (unless the implementation specifies otherwise).
    ///
    /// @since 1.0
    public AbstractBreakableBuilder() {
        this.breaks = new HashSet<>();
        this.methodStatuses = new HashMap<>();
    }

    /// Creates a new `AbstractBreakableBuilder` by copying configuration from another builder.
    ///
    /// This constructor performs a defensive copy of the breaks and method statuses.
    ///
    /// @param other the builder to copy from; must not be null
    /// @throws NullPointerException if `other` is null
    /// @since 1.0
    public AbstractBreakableBuilder(final @NonNull AbstractBreakableBuilder<B, C> other) {
        this.breaks = new HashSet<>(other.breaks);
        this.methodStatuses = new HashMap<>(other.methodStatuses);
        this.isSafe = other.isSafe;
    }

    /// Creates a new `AbstractBreakableBuilder` with the specified breaks and method statuses.
    ///
    /// @param breaks the initial set of breaks; must not be null
    /// @param methodStatuses the initial method statuses; must not be null
    /// @throws NullPointerException if `breaks` or `methodStatuses` is null
    /// @since 1.0
    public AbstractBreakableBuilder(final @NonNull Set<Break> breaks,
                                    final @NonNull Map<InterfaceMethod, MethodStatus> methodStatuses) {
        this.breaks = breaks;
        this.methodStatuses = methodStatuses;
    }

    /// Creates a new `AbstractBreakableBuilder` with the specified breaks, method statuses, and thread safety.
    /// @param breaks the initial set of breaks; must not be null
    /// @param methodStatuses the initial method statuses; must not be null
    /// @param isSafe whether the resulting object is safe for concurrent access.
    public AbstractBreakableBuilder(final @NonNull Set<Break> breaks,
                                    final @NonNull Map<InterfaceMethod, MethodStatus> methodStatuses,
                                    final boolean isSafe) {
        this.breaks = breaks;
        this.methodStatuses = methodStatuses;
        this.isSafe = isSafe;
    }

    /// Returns this builder instance for method chaining.
    ///
    /// This method is used by the fluent API to return the correctly typed builder instance
    /// from methods defined in this abstract class.
    ///
    /// @return this builder instance
    /// @since 1.0
    public abstract B self();

    /// Creates a copy of this builder.
    ///
    /// @return a new builder instance that is a copy of this one
    /// @since 1.0
    public abstract B copy();

    /// Builds the breakable object with the current configuration.
    ///
    /// @return a new breakable instance
    /// @since 1.0
    public abstract C build();

    /// Adds a break to this builder.
    ///
    /// @param aBreak the break to add_singleElement_returnsTrueAndUpdatesSize; must not be null
    /// @return this builder for method chaining
    /// @throws NullPointerException if `aBreak` is null
    /// @since 1.0
    public final B addBreak(final @NonNull Break aBreak) {
        breaks.add(aBreak);
        return self();
    }

    public final B addBreaks(final @NonNull Collection<Break> moreBreaks) {
        breaks.addAll(moreBreaks);
        return self();
    }

    /// Configures the builder so that the resulting object does not support the specified method.
    ///
    /// @param method the optional method that should not be supported; must not be null
    /// @return this builder for method chaining
    /// @throws NullPointerException if `method` is null
    /// @since 1.0
    public final @NonNull B doesNotSupport(final @NonNull InterfaceMethod method) {
        methodStatuses.put(method, new MethodStatus(false));
        return self();
    }

    public final @NonNull B setMethodStatuses(final @NonNull Map<InterfaceMethod, MethodStatus> moreStatuses) {
        methodStatuses.putAll(moreStatuses);
        return self();
    }

    /// Returns the breaks that have been added to the builder.
    ///
    /// @return the set of breaks for the breakable object
    /// @since 1.0
    public @NonNull Set<Break> breaks() {
        return breaks;
    }

    /// Returns the method status configurations that have been added to the builder.
    ///
    /// @return the map of method configurations
    /// @since 1.0
    protected @NonNull Map<InterfaceMethod, MethodStatus> methodStatuses() {
        return methodStatuses;
    }

    /// Sets whether the resulting object is safe for concurrent access.
    /// @param newSafe whether the resulting object is safe for concurrent access.
    /// @return this builder for method chaining.
    public final B setSafe(final boolean newSafe) {
        this.isSafe = newSafe;
        return self();
    }

    /// Returns whether the resulting object is safe for concurrent access.
    /// @return whether the resulting object is safe for concurrent access.
    public final boolean isSafe() {
        return isSafe;
    }
}
