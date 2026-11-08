package com.soliscode.test.breakable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.MethodStatus;

/// Base builder class for creating breakable objects with configurable breaks and method support.
///
/// This abstract class provides the core functionality for builders that create `Breakable`
/// instances. It maintains a set of `Break` objects to be applied and a map of `InterfaceMethod`
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
/// @author evanbergstrom
/// @since 1.0.0
public abstract class AbstractBreakableBuilder<B extends AbstractBreakableBuilder<B, C>, C extends Breakable>
        implements BreakableBuilder<C> {

    /// The set of breaks that will be applied to the breakable object.
    private final Set<Break> breaks;

    /// The set of optional methods that will not be supported by the breakable object.
    private final Map<InterfaceMethod, MethodStatus> methodStatuses;

    /// Whether the breakable object is safe for concurrent access.
    private boolean isSafe = false;

    /// Creates a new `AbstractBreakableBuilder` with an empty configuration.
    ///
    /// The resulting builder will have no breaks and all optional methods will be supported
    /// by default (unless the implementation specifies otherwise).
    ///
    /// @since 1.0.0
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
    /// @since 1.0.0
    public AbstractBreakableBuilder(final AbstractBreakableBuilder<B, C> other) {
        this.breaks = new HashSet<>(other.breaks);
        this.methodStatuses = new HashMap<>(other.methodStatuses);
        this.isSafe = other.isSafe;
    }

    /// Creates a new `AbstractBreakableBuilder` with the specified breaks and method statuses.
    ///
    /// @param breaksArg the initial set of breaks; must not be null
    /// @param methodStatusesArg the initial method statuses; must not be null
    /// @throws NullPointerException if `breaks` or `methodStatuses` is null
    /// @since 1.0.0
    public AbstractBreakableBuilder(final Set<Break> breaksArg,
                                    final Map<InterfaceMethod, MethodStatus> methodStatusesArg) {
        this.breaks = breaksArg;
        this.methodStatuses = methodStatusesArg;
    }

    /// Creates a new `AbstractBreakableBuilder` with the specified breaks, method statuses, and thread safety.
    ///
    /// @param breaksArg the initial set of breaks; must not be null
    /// @param methodStatusesArg the initial method statuses; must not be null
    /// @param isSafeArg whether the resulting object is safe for concurrent access
    /// @throws NullPointerException if `breaks` or `methodStatuses` is null
    public AbstractBreakableBuilder(final Set<Break> breaksArg,
                                    final Map<InterfaceMethod, MethodStatus> methodStatusesArg,
                                    final boolean isSafeArg) {
        this.breaks = breaksArg;
        this.methodStatuses = methodStatusesArg;
        this.isSafe = isSafeArg;
    }

    @Override
    public abstract B self();

    @Override
    public final B addBreak(final Break aBreak) {
        breaks.add(aBreak);
        return self();
    }

    @Override
    public final B addBreaks(final Collection<Break> moreBreaks) {
        breaks.addAll(moreBreaks);
        return self();
    }

    @Override
    public final B doesNotSupport(final InterfaceMethod method) {
        methodStatuses.put(method, new MethodStatus(false));
        return self();
    }

    @Override
    public final B setMethodStatuses(final Map<InterfaceMethod, MethodStatus> moreStatuses) {
        methodStatuses.putAll(moreStatuses);
        return self();
    }

    @Override
    public Set<Break> breaks() {
        return breaks;
    }

    @Override
    public Map<InterfaceMethod, MethodStatus> methodStatuses() {
        return methodStatuses;
    }

    @Override
    public final B setSafe(final boolean newSafe) {
        this.isSafe = newSafe;
        return self();
    }

    @Override
    public final boolean isSafe() {
        return isSafe;
    }
}
