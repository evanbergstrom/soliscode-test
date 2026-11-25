/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.object;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.contract.support.ContractSupport;
import com.soliscode.test.methods.ObjectMethods;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// **Contract for the `Object#hashCode()` method**
///
/// This interface defines tests for the `hashCode()` method as specified in [Object].
/// It verifies stability and equality consistency, as [Object] requires, and, more strictly, distribution quality.
///
/// ## Purpose
/// The purpose of this contract is to ensure that a class's `hashCode()` implementation follows the
/// contract defined by [Object#hashCode()], which is essential for correct behavior in hash-based
/// collections like [java.util.HashMap] and [java.util.HashSet].
///
/// ## Usage Examples
/// This contract is normally used through the [ObjectContract] class:
///
/// ```java
/// public class MyClassTest extends AbstractTest implements ObjectContract<MyClass> {
///     // provider(), as in ObjectContract's example
/// }
/// ```
///
/// If a class does not implement `hashCode()` according to the [Object] specification, it can
/// be omitted using the `doesNotSupportMethod()` method:
///
/// ```java
/// public class MyClassTest extends AbstractTest implements ObjectContract<MyClass> {
///     // provider(), as in ObjectContract's example
///
///     public MyClassTest() {
///         doesNotSupportMethod(ObjectMethods.HASH_CODE);
///     }
/// }
/// ```
///
/// ## Thread Safety
/// This contract interface does not provide any thread-safety guarantees. The thread safety of the
/// tests depends on the object and [com.soliscode.test.provider.ObjectProvider] implementations being tested.
///
/// @param <T> The type being tested.
/// @author evanbergstrom
/// @see Object#hashCode()
/// @see ObjectContract
/// @since 1.0.0
public interface HashCodeMethodContract<T> extends ContractSupport<T> {

    /// How far, as a fraction, the occupancy of a hash table filled with unequal values may fall below what a
    /// uniform hash gives, in [#hashCodeWhenCalledManyTimesHasUniformDistribution()]: at 0.10, the table must have at
    /// least 90% of the occupied buckets a uniform hash would fill. Until 0.1.7 it was an allowance on a load factor
    /// that the test could not miss (#70).
    double LOAD_FACTOR_ALLOWANCE = 0.10;

    /// Returns whether the class under test spreads the hash codes of unequal values, as a hash table needs, so that
    /// the two tests that read [Object#hashCode()] more strictly than it is written run.
    ///
    /// [Object#hashCode()] says that unequal objects are *not required* to have distinct hash codes, and that
    /// distinct ones "may improve the performance of hash tables". [#hashCodeWithUniqueValuesReturnsUniqueIntegers()]
    /// and [#hashCodeWhenCalledManyTimesHasUniformDistribution()] expect them. A class whose hash code is legitimately
    /// constant, or coarse, returns `false`, and the other tests of `hashCode` still run.
    ///
    /// @return `true` unless overridden
    /// @since 0.1.7
    default boolean hashCodeSpreadsUnequalValues() {
        return true;
    }

    /// Tests that the `hashCode()` method consistently returns the same integer value over multiple
    /// invocations on the same instance.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#hashCode()
    @Test
    @DisplayName("hashCode() returns the same integer for multiple invocations")
    default void hashCodeWhenRepeatedIsConsistent() {
        if (supportsMethod(ObjectMethods.HASH_CODE)) {
            T value = provider().createInstance();
            int hash1 = value.hashCode();
            int hash2 = value.hashCode();
            assertEquals(hash1, hash2);
        }
    }

    /// Tests that the `hashCode()` method returns the same integer value for objects that are equal.
    /// It runs only where [com.soliscode.test.contract.support.ContractSupport#equalsComparesValues] is `true`, since
    /// it compares a value with its copy.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#hashCode()
    @Test
    @DisplayName("hashCode() returns the same integer for equal values")
    default void hashCodeWithEqualValuesReturnsSameInteger() {
       if (supportsMethod(ObjectMethods.HASH_CODE) && equalsComparesValues()) {
           T value = provider().createInstance();
           T other = provider().copyInstance(value);
           int hash = value.hashCode();
           int otherHash = other.hashCode();
           assertEquals(hash, otherHash);
       }
    }

    /// Tests that the `hashCode()` method returns different values for objects that are not equal.
    ///
    /// This reads [Object#hashCode()] more strictly than it is written: "It is *not* required that if two objects are
    /// unequal according to the equals method, then calling the hashCode method on each of the two objects must
    /// produce distinct integer results." It runs only where [#hashCodeSpreadsUnequalValues()] is `true`.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#hashCode()
    @Test
    @DisplayName("hashCode() returns different integers for unequal values")
    default void hashCodeWithUniqueValuesReturnsUniqueIntegers() {
        if (supportsMethod(ObjectMethods.HASH_CODE) && hashCodeSpreadsUnequalValues()) {
            List<T> values = provider().createUniqueInstances(10);
            long uniqueValues = values.stream().map(Object::hashCode).distinct().count();
            assertEquals(values.size(), uniqueValues);
        }
    }

    /// Tests that the hash codes of unequal values spread across a hash table laid out as [java.util.HashMap] lays it
    /// out: 1000 values in 2048 buckets, each indexed by `(h ^ (h >>> 16)) & 2047`. The test counts the occupied
    /// buckets, and requires at least `1 - LOAD_FACTOR_ALLOWANCE` of the 791.2 that a uniform hash fills
    /// (`m(1 - e^(-n/m))`), 712.06, so 713.
    ///
    /// The values are `provider().createInstance(seed)` for distinct seeds below
    /// `min(100_000, provider().uniqueSizeLimit())`, drawn from a [Random] of a fixed seed. They are the same on every
    /// run, and are not biased by consecutive seeds, which give `String` 662 buckets. The provider's `createInstance`
    /// must accept every seed in that range. Only distinct values count, by `equals` (by seed, where `EQUALS` is
    /// unsupported): a provider whose seeds alias, as
    /// `ShortProvider`'s do above 32,767, gives fewer values than seeds. A provider that yields fewer than 1000
    /// distinct values in 10,000 draws, or whose limit is below 1000, has too few for the test, which then does not
    /// run.
    ///
    /// This reads [Object#hashCode()] more strictly than it is written, which says only that distinct hash codes "may
    /// improve the performance of hash tables". It runs only where [#hashCodeSpreadsUnequalValues()] is `true`.
    ///
    /// @throws org.opentest4j.AssertionFailedError if the test fails.
    /// @see Object#hashCode()
    @Test
    @DisplayName("hashCode() returns integers that spread across a hash table")
    default void hashCodeWhenCalledManyTimesHasUniformDistribution() {
        final int values = 1000;
        final int buckets = 2048;
        final int seedBound = 100_000;
        final long randomSeed = 70L;
        final int spreadShift = 16;
        final int drawsPerValue = 10;
        if (!supportsMethod(ObjectMethods.HASH_CODE) || !hashCodeSpreadsUnequalValues()) {
            return;
        }
        long seeds = Math.min(seedBound, provider().uniqueSizeLimit());
        if (seeds < values) {
            return;
        }
        Random random = new Random(randomSeed);
        Set<Long> drawn = new HashSet<>();
        List<T> distinct = new ArrayList<>();
        boolean compareValues = supportsMethod(ObjectMethods.EQUALS);
        while (distinct.size() < values && drawn.size() < values * drawsPerValue && drawn.size() < seeds) {
            long seed = random.nextLong(seeds);
            if (drawn.add(seed)) {
                T value = provider().createInstance(seed);
                if (!compareValues || !distinct.contains(value)) {
                    distinct.add(value);
                }
            }
        }
        if (distinct.size() < values) {
            return;
        }
        boolean[] occupied = new boolean[buckets];
        int count = 0;
        for (final T value : distinct) {
            int h = value.hashCode();
            int index = (h ^ (h >>> spreadShift)) & (buckets - 1);
            if (!occupied[index]) {
                occupied[index] = true;
                count++;
            }
        }
        double uniform = buckets * (1 - Math.exp(-(double) values / buckets));
        double required = (1 - LOAD_FACTOR_ALLOWANCE) * uniform;
        int filled = count;
        assertTrue(filled >= required, () -> values + " unequal values filled " + filled + " of " + buckets
                + " buckets; a uniform hash fills " + Math.round(uniform) + ", and at least " + required
                + " are required");
    }
}
