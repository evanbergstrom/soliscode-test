package com.soliscode.test.contract.support;

import java.util.Collection;

import com.soliscode.test.provider.CollectionProvider;
import com.soliscode.test.provider.CollectionProviders;

/// Mixin for a collection contract class that provides a collection objectProvider for synchronized collection
/// instances.
///
/// This interface is intended to be used by test classes that verify the contract of
/// synchronized collections. It simplifies the setup by providing a default implementation
/// of the `objectProvider()` method that uses `CollectionProviders#provideSynchronizedCollection`.
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
public interface WithSynchronizedCollection<E> extends CollectionProviderSupport<Collection<E>, E> {

    /// Returns a collection objectProvider for synchronized collection instances.
    ///
    /// This implementation uses `CollectionProviders#provideSynchronizedCollection`
    /// with the `elementProvider()` to create the objectProvider.
    ///
    /// @return a synchronized collection objectProvider
    @Override
    default CollectionProvider<Collection<E>, E> provider() {
        return CollectionProviders.provideSynchronizedCollection(elementProvider());
    }
}
