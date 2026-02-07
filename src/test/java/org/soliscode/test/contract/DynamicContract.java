package org.soliscode.test.contract;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.InterfaceMethod;
import org.soliscode.test.MethodStatus;
import org.soliscode.test.breakable.Break;
import org.soliscode.test.contract.support.ProviderSupport;
import org.soliscode.test.provider.ObjectProvider;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/// A base class for contracts that are created dynamically for testing purposes.
///
/// This class provides support for creating contract instances with specific breaks or
/// method support configurations. It is used to generate tests for implementations
/// of various interfaces (e.g., `Collection`, `Map`) where different behavioral
/// anomalies need to be systematically tested.
///
/// @param <T> the type of object being tested.
/// @param <P> the type of provider used to create instances of the test object.
/// @author evanbergstrom
/// @since 1.0
public abstract class DynamicContract<T, P extends ObjectProvider<T>> extends AbstractTest
            implements ProviderSupport<T> {

    /// A constant for an empty set of breaks.
    public static final Set<Break> EMPTY_BREAKS = Collections.emptySet();

    /// A constant for an empty map of method statuses.
    public static final Map<InterfaceMethod, MethodStatus> EMPTY_METHOD_STATUSES = Collections.emptyMap();

    /// The set of breaks applied to this contract.
    private final Set<Break> breaks;

    /// The map of method statuses applied to this contract.
    private final Map<InterfaceMethod, MethodStatus> methodStatuses;

    /// The function used to create a provider instance.
    private final ProviderCreator<T, P> providerCreator;

    /// Creates a new `DynamicContract` with a single break.
    ///
    /// @param b the break to apply; may be null.
    /// @param m the method that is not supported; may be null.
    /// @param providerCreator the function used to create a provider; must not be null.
    public DynamicContract(final Break b, final InterfaceMethod m, final ProviderCreator<T, P> providerCreator) {
        this.breaks = (b == null) ? EMPTY_BREAKS : Set.of(b);
        this.methodStatuses = (m == null) ? EMPTY_METHOD_STATUSES : Map.of(m, MethodStatus.UNSUPPORTED);
        this.providerCreator = providerCreator;
        if (m != null) {
            doesNotSupportMethod(m);
        }
    }

    /// Returns the provider for instances of the class being tested.
    ///
    /// @return the provider instance.
    @Override
    public @NonNull P provider() {
        return providerCreator.create(breaks, methodStatuses, this);
    }

    /// Functional interface for creating provider instances.
    ///
    /// @param <T> the type of object being tested.
    /// @param <P> the type of provider.
    @FunctionalInterface
    public interface ProviderCreator<T, P extends ObjectProvider<T>> {
        /// Creates a provider instance.
        ///
        /// @param breaks the set of breaks to apply.
        /// @param statuses the map of method statuses to apply.
        /// @param contract the contract instance requesting the provider.
        /// @return the created provider.
        P create(Set<Break> breaks, Map<InterfaceMethod, MethodStatus> statuses, DynamicContract<T, P> contract);
    }
}
