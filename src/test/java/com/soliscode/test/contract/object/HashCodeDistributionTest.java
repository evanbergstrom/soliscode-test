/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.contract.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import com.soliscode.test.AbstractTest;
import com.soliscode.test.methods.ObjectMethods;
import com.soliscode.test.provider.ObjectProvider;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests of `HashCodeMethodContract.hashCodeWhenCalledManyTimesHasUniformDistribution` at the edges of its sampling
/// (#70). The values have a constant hash code, which fills one bucket, so where the test runs it fails, and where it
/// passes it did not run.
@DisplayName("Tests of the hash code distribution test's sampling")
public class HashCodeDistributionTest {

    /// How many values the distribution test needs.
    private static final int VALUES = 1000;

    /// Default constructor.
    public HashCodeDistributionTest() {
        super();
    }

    /// A value whose hash code is constant.
    ///
    /// @param value the value
    record Clumped(long value) {
        @Override
        public boolean equals(final Object o) {
            return o instanceof Clumped other && other.value == value;
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }

    /// The `hashCode` contract on a provider of [Clumped] values.
    static class Subject extends AbstractTest implements HashCodeMethodContract<Clumped> {
        private final ObjectProvider<Clumped> clumped;

        Subject(final ObjectProvider<Clumped> clumpedArg) {
            this.clumped = clumpedArg;
        }

        @Override
        public ObjectProvider<Clumped> provider() {
            return clumped;
        }
    }

    /// A provider whose seed `s` gives `Clumped(s % modulus)`, with the given limit.
    private static Subject aliasing(final long modulus, final long limit) {
        return new Subject(new ObjectProvider<>() {
            @Override
            public Clumped defaultInstance() {
                return new Clumped(0);
            }

            @Override
            public Clumped createInstance(final long seed) {
                return new Clumped(seed % modulus);
            }

            @Override
            public Clumped copyInstance(final Clumped other) {
                return new Clumped(other.value());
            }

            @Override
            public long uniqueSizeLimit() {
                return limit;
            }
        });
    }

    /// A provider that fails the test if any of its methods is called.
    private static ObjectProvider<Clumped> untouchable() {
        return new ObjectProvider<>() {
            @Override
            public Clumped defaultInstance() {
                throw new AssertionError("the provider was touched");
            }

            @Override
            public Clumped createInstance(final long seed) {
                throw new AssertionError("the provider was touched");
            }

            @Override
            public Clumped copyInstance(final Clumped other) {
                throw new AssertionError("the provider was touched");
            }

            @Override
            public long uniqueSizeLimit() {
                throw new AssertionError("the provider was touched");
            }
        };
    }

    @Test
    @DisplayName("It runs over seeds that alias, counting each distinct value once")
    void runsOverAliasingSeeds() {
        assertThrows(AssertionFailedError.class,
                () -> aliasing(2 * VALUES, Long.MAX_VALUE).hashCodeWhenCalledManyTimesHasUniformDistribution());
    }

    @Test
    @DisplayName("It runs where the limit is exactly the 1000 values it needs")
    void runsAtALimitOfExactlyEnough() {
        assertThrows(AssertionFailedError.class,
                () -> aliasing(Long.MAX_VALUE, VALUES).hashCodeWhenCalledManyTimesHasUniformDistribution());
    }

    @Test
    @DisplayName("It does not run where 10,000 seeds give fewer than 1000 distinct values")
    void skipsTooFewDistinctValues() {
        assertDoesNotThrow(
                () -> aliasing(VALUES / 2, Long.MAX_VALUE).hashCodeWhenCalledManyTimesHasUniformDistribution());
    }

    @Test
    @DisplayName("It does not run where every seed is drawn before 1000 distinct values are found")
    void skipsWhenTheSeedsRunOut() {
        assertDoesNotThrow(() -> aliasing(VALUES - 1, VALUES).hashCodeWhenCalledManyTimesHasUniformDistribution());
    }

    @Test
    @DisplayName("It does not run where the limit is below 1000")
    void skipsALimitBelowTheValuesItNeeds() {
        assertDoesNotThrow(
                () -> aliasing(Long.MAX_VALUE, VALUES - 1).hashCodeWhenCalledManyTimesHasUniformDistribution());
    }

    /// A value whose `equals` is unsupported, and whose hash code is constant.
    static final class NoEquals {
        @Override
        public boolean equals(final Object o) {
            throw new UnsupportedOperationException("equals is unsupported");
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }

    @Test
    @DisplayName("It does not call equals where equals is unsupported, and counts each seed once")
    void doesNotCallUnsupportedEquals() {
        HashCodeMethodContract<NoEquals> subject = new NoEqualsSubject();
        AssertionFailedError e = assertThrows(AssertionFailedError.class,
                subject::hashCodeWhenCalledManyTimesHasUniformDistribution);
        assertTrue(e.getMessage().contains("filled 1 of"), () -> "it ran on the hash codes, not on equals: " + e);
    }

    /// The `hashCode` contract on [NoEquals], with `equals` unsupported.
    static final class NoEqualsSubject extends AbstractTest implements HashCodeMethodContract<NoEquals> {
        NoEqualsSubject() {
            doesNotSupportMethod(ObjectMethods.EQUALS);
        }

        @Override
        public ObjectProvider<NoEquals> provider() {
            return new ObjectProvider<>() {
                @Override
                public NoEquals defaultInstance() {
                    return new NoEquals();
                }

                @Override
                public NoEquals createInstance(final long seed) {
                    return new NoEquals();
                }

                @Override
                public NoEquals copyInstance(final NoEquals other) {
                    return other;
                }
            };
        }
    }

    @Test
    @DisplayName("It does not touch the provider where hashCode is unsupported or its opt-out is off")
    void doesNotTouchTheProviderWhenOff() {
        Subject unsupported = new Subject(untouchable());
        unsupported.doesNotSupportMethod(ObjectMethods.HASH_CODE);
        assertDoesNotThrow(unsupported::hashCodeWhenCalledManyTimesHasUniformDistribution);
        Subject optedOut = new Subject(untouchable()) {
            @Override
            public boolean hashCodeSpreadsUnequalValues() {
                return false;
            }
        };
        assertDoesNotThrow(optedOut::hashCodeWhenCalledManyTimesHasUniformDistribution);
    }
}
