/*
 * Copyright 2024 Evan Bergstrom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.soliscode.test.provider;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

/// Provides [Object] instances for use in testing.
///
/// @implSpec
/// Implementations of this interface must provide two methods:
/// - [createObject(int)][#createObject(int)]
/// - [copyObject()][#copyObject()]
///
/// @param <T> the type being tested.
/// @author evanbergstrom
/// @since 1.0.0
public interface ObjectProvider<T> {

    /// Creates a single instance of class being tested using the default constructor.
    /// @return  an instance of class.
    @NotNull T defaultInstance();

    /// Creates a single instance of class being tested based upon an integer seed value.
    ///
    /// @implSpec
    /// Calls to this method with different seed values should return objects with different values.
    ///
    /// @param   seed an integer seed to use to create and instance of the class.
    /// @return  an instance of class.
    @NotNull T createInstance(int seed);

    /// Copies an instance of the class being tested. The copy will satisfy equality semantics with the original
    /// instance being copied.
    ///
    /// @implSpec
    /// The copy of the object should satisfy the equality semantics with the original object (_i.e._ for `o1` and
    /// `o2`, `o1.equals(o2)` should return `true` and `o1.hashCode() == o2.hashCode()` should evaluate to `true`.
    ///
    /// @param      other the instance to copy.
    /// @return     a copy of the instance.
    /// @throws     NullPointerException if the argument is a null value.
    /// @complexity _constant time_.
    @NotNull T copyInstance(@NotNull T other);

    /// Creates a single instance of class being tested.
    ///
    /// @implSpec
    /// Each call must return a valid object instance, but there is no requirement that successive calls to the method
    /// return instances that satisfy equality semantics, or that they be unique. The default implementation is the
    /// same as calling `createInstance(0)`.
    ///
    /// @return     an instance of class.
    /// @complexity _constant time_.
    default @NotNull T createInstance(){
        return createInstance(0);
    }

    /// Returns the number of unique instances of the class that can be created.
    ///
    /// @implSpec
    /// The default implementation returns [Integer#MAX_VALUE]. This should be overridden for enumerations or any classes
    /// that have a bounded number of unique values.
    ///
    /// @return     the maximum number of unique instances that can be created.
    /// @complexity _constant time_.
    @SuppressWarnings("SameReturnValue")
    default int uniqueSizeLimit() {
        return Integer.MAX_VALUE;
    }

    /// Creates a [Supplier] that will generate equal instances of the class being provided.
    ///
    /// @implSpec
    /// The supplier should produce equal instances of the class for each the call to `get()`.
    ///
    /// @implNote
    /// The default implementation will call `createInstance(0)` to create each instance.
    ///
    /// @return     A supplier the returns instances of the class that are equal.
    /// @complexity _constant time_.
    default @NotNull Supplier<T> equalInstanceSupplier() {
        final ObjectProvider<T> provider = this;
        return new Supplier<>() {
            private final T orig = provider.createInstance(0);

            @Override
            public T get() {
                return provider.copyInstance(orig);
            }
        };
    }

    /// Creates a [Supplier] that will generate unique instances of the class being provided. Uniqueness will be
    /// guaranteed for all instances created by this supplier. Instances may not be unique from multiple suppliers.
    ///
    /// @implSpec
     /// The supplier should produce unique instances of the class for each the call to `get()`, and it should be callable
     /// a number of times up to the value returned by `uniqueSizeLimit()`. If it is called more that `uniqueSizeLimit`
     /// times, it should throw an [IllegalStateException].
     ///
     /// @implNote
     /// The default implementation will call `createInstance(int)` with successive integers starting with 0.
     ///
     /// @return A supplier the returns instances of the class that are unique.
     /// @throws IllegalStateException if `get` is called on the supplier more than `uniqueSizeLimit` times.
     /// @complexity _constant time_.
    default @NotNull Supplier<T> uniqueInstanceSupplier() {
        return uniqueInstanceSupplier(0);
    }

    /// Creates a [Supplier] that will generate unique instances of the class being provided. Uniqueness will be
    /// guaranteed for all instances created by this supplier. Instances may not be unique from multiple suppliers.
    ///
    /// @implSpec
    /// The supplier should produce unique instances of the class for each the call to `get()`, and it should be callable
    /// a number of times up to the value returned by `uniqueSizeLimit()`. If it is called more that `uniqueSizeLimit`
    /// times, it should throw an [IllegalStateException].
    ///
    /// @implNote
    /// The default implementation will call `createInstance(int)` with successive integers starting with 0.
    ///
    /// param seed the initial seed to use to crate the first instance.
    /// @param seed the seed to use to create elements.
    /// @return A supplier the returns instances of the class that are unique.
    /// @throws IllegalStateException if `get` is called on the supplier more than `uniqueSizeLimit` times.
    /// @complexity _constant time_.
    default @NotNull Supplier<T> uniqueInstanceSupplier(final int seed) {
        final ObjectProvider<T> provider = this;
        return new Supplier<>() {
            private int i = seed;

            @Override
            public T get() {
                if (i >= uniqueSizeLimit()) {
                    throw new IllegalStateException("cannot create " + i + " unique instances, limit is " + uniqueSizeLimit());
                }
                return provider.createInstance(i++);
            }
        };
    }

    /// Creates a [Supplier] that will generate random instances of the class being provided. This should be used where
    /// the individual values of the instances is not important, such here we are testing for performance of some part
    /// of the implementation, but where creating instances with successive seed values might bias the algorithm (_e.g._
    /// hash code distribution). In most cases, the methods [#uniqueObjectSupplier] should be
    /// preferred so that the test data is stable across executions.
    ///
    /// @implSpec
    /// The supplier should produce random instances of the class for each the call to `get()`. The order of the objects
    /// created should be stable across instances of the supplier.
    ///
    /// @implNote
    /// The default implementation will call `createInstance(int)` with random integers between 0 and the value returned
    /// by `upperUniqueLimit()`.
    ///
    /// @return A supplier the returns random instances of the class.
    default @NotNull Supplier<T> randomInstanceSupplier() {
        final ObjectProvider<T> provider = this;
        return new Supplier<>() {
            private final Random r = new Random();

            @Override
            public T get() {
                return provider.createInstance(r.nextInt(uniqueSizeLimit()));
            }
        };
    }

    /// Creates s list of instances that are equal to each other.
    ///
    /// @param size The number of equal instances to create.
    /// @return A list of equal instances.
    /// @complexity _linear time_ based upon the number of instance (the `size` argument).
    default @NotNull List<T> createEqualObjects(int size) {
        return Stream.generate(equalInstanceSupplier()).limit(size).toList();
    }

    /// Creates multiple instances of class being tested with unique values.
    ///
    /// @param size the number of instances to create.
     /// @return a list of the created instances.
     /// @throws IllegalArgumentException if size is greater than `uniqueSizeLimit()`
     /// @complexity _linear time_ based upon the number of instance (the `size` argument).
    default @NotNull List<T> createUniqueInstances(int size) {
        return createUniqueInstances(size, 0);
    }

    /// Creates multiple instances of class being tested with unique values.
    ///
    /// @param size the number of instances to create.
    /// @param seed the seed to use to create elements.
    /// @return a list of the created instances.
    /// @throws IllegalArgumentException if size is greater than `uniqueSizeLimit()`
    /// @complexity _linear time_ based upon the number of instance (the `size` argument).
    default @NotNull List<T> createUniqueInstances(int size, int seed) {
        if (size > uniqueSizeLimit()) {
            throw new IllegalArgumentException("cannot create " + size + " unique instances, limit is " + uniqueSizeLimit());
        }
        return new ArrayList<>(Stream.generate(uniqueInstanceSupplier(seed)).limit(size).toList());
    }

    /// Creates multiple instances of class being tested with random values. This should be used where the individual
    /// values of the instances is not important, such as here we are testing for performance of some part of the
    /// implementation (_e.g._ hash code distribution). In most cases, the method [#createUniqueObjects] should be
    /// preferred so that the test data is stable across executions.
    ///
    /// @param size the number of instances to create.
    /// @return a list of the created instances.
    /// @complexity _linear time_ based upon the number of instance (the `size` argument).
    default @NotNull List<T> createRandoInstances(int size) {
        return new ArrayList<>(Stream.generate(randomInstanceSupplier()).limit(size).toList());
    }
}
