/// Test suites and contract definitions for the [Queue][java.util.Queue] interface.
///
/// This package provides a comprehensive set of contract tests for the [Queue][java.util.Queue]
/// interface, following the design principles of the SolisCode Test library. It includes
/// individual contract interfaces for each [Queue][java.util.Queue] method and a composite
/// [QueueContract][com.soliscode.test.contract.queue.QueueContract] that aggregates them.
///
/// ## Purpose
/// The primary purpose of this package is to enable systematic verification of
/// [Queue][java.util.Queue] implementations. By providing standardized tests for both
/// normal and exceptional behaviors (including optional method support), it ensures
/// that queue implementations adhere to the JDK specification.
///
/// ## Package Components
///
/// ### Method Contracts
/// Individual interfaces for testing specific [Queue][java.util.Queue] methods:
/// - [com.soliscode.test.contract.queue.ElementContract]: Tests for [java.util.Queue#element()]
/// - [com.soliscode.test.contract.queue.OfferContract]: Tests for [java.util.Queue#offer(Object)]
/// - [com.soliscode.test.contract.queue.PeekContract]: Tests for [java.util.Queue#peek()]
/// - [com.soliscode.test.contract.queue.PollContract]: Tests for [java.util.Queue#poll()]
/// - [com.soliscode.test.contract.queue.RemoveContract]: Tests for [java.util.Queue#remove()]
///
/// ### Interface Contract
/// - [QueueContract][com.soliscode.test.contract.queue.QueueContract]: A unified contract interface that extends all
///   method contracts and [CollectionContract][com.soliscode.test.contract.collection.CollectionContract].
///
/// ### Support Classes
/// - [QueueMethods][com.soliscode.test.contract.queue.QueueMethods]: Enum used to identify [Queue][java.util.Queue]
///   methods for method support configuration.
///
/// ## Usage Examples
///
/// ### Implementing a Queue Test
/// To test a custom [Queue][java.util.Queue] implementation, create a test class that implements
/// [com.soliscode.test.contract.queue.QueueContract] and provides a
/// [CollectionProvider][com.soliscode.test.provider.CollectionProvider]:
///
/// ```java
/// public class MyQueueTest
///         extends AbstractTest
///         implements QueueContract<String, MyQueue<String>>,
///                    WithProvider<String> {
///
///     @Override
///     public CollectionProvider<String, MyQueue<String>> objectProvider() {
///         return CollectionProviders.from(
///             MyQueue::new,
///             MyQueue::new,
///             MyQueue::new,
///             elementProvider()
///         );
///     }
/// }
/// ```
///
/// ## Thread Safety
/// The contract interfaces themselves are thread-safe. However, the thread safety
/// of the tests depends on the [Queue][java.util.Queue] implementation being tested and the
/// [CollectionProvider][com.soliscode.test.provider.CollectionProvider] provided. Most contracts assume
/// single-threaded access during test execution unless otherwise documented.
///
/// @author evanbergstrom
/// @since 1.0
@NullMarked
package com.soliscode.test.contract.queue;

import org.jspecify.annotations.NullMarked;
