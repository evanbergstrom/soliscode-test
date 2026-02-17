package org.soliscode.test.contract.support;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.provider.CollectionProvider;
import org.soliscode.test.provider.CollectionProviders;

import java.util.Collection;

/// Mixin for a collection contract class that provides a collection provider for synchronized collection instances.
///
/// This interface is intended to be used by test classes that verify the contract of
/// synchronized collections. It simplifies the setup by providing a default implementation
/// of the `provider()` method that uses `CollectionProviders#provideSynchronizedCollection`.
///
/// ## Usage Example
/// ```java
/// public class SynchronizedCollectionTest extends AbstractTest
///     implements CollectionContract<Integer, Collection<Integer>>,
///                WithSynchronizedCollection<Integer>,
///                WithIntegerElement {
/// }
/// ```
///
/// @param <E> the type of elements in the collection
/// @author evanbergstrom
/// @since 1.0
/// @see CollectionProvider
/// @see java.util.Collections#synchronizedCollection(Collection)
public interface WithSynchronizedCollection<E> extends CollectionProviderSupport<E, Collection<E>> {

    /// Returns a collection provider for synchronized collection instances.
    ///
    /// This implementation uses `CollectionProviders#provideSynchronizedCollection`
    /// with the `elementProvider()` to create the provider.
    ///
    /// @return a synchronized collection provider
    @Override
    default @NonNull CollectionProvider<E, Collection<E>> provider() {
        return CollectionProviders.provideSynchronizedCollection(elementProvider());
    }
}
