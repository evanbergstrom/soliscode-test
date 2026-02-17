package org.soliscode.test.util;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.numeric.IntegerContract;
import org.soliscode.test.provider.IntegerNumberProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/// **Tests for the `UncachedInteger` class**
///
/// This class provides tests for the [UncachedInteger] utility class, ensuring it
/// behaves as expected, particularly regarding its lack of value caching.
/// It also implements the [IntegerContract] to verify that [UncachedInteger]
/// fulfills the requirements of an integer-like object in the SolisCode test framework.
///
/// ## Test Scope
/// - **Value Retrieval**: Verifies that `valueOf()` correctly converts primitive integers.
/// - **Identity Verification**: Ensures that `valueOf()` returns distinct instances for the same value.
/// - **Contract Compliance**: Validates compliance with [IntegerContract].
///
/// @author evanbergstrom
/// @see UncachedInteger
/// @see IntegerContract
/// @since 1.0.0
public class UncachedIntegerTest extends AbstractTest implements IntegerContract<UncachedInteger> {

    /// Returns the provider for creating and managing `UncachedInteger` instances.
    ///
    /// The provider uses an anonymous [IntegerNumberProvider] implementation to
    /// wrap [UncachedInteger] instantiation and state tracking.
    ///
    /// @return an [IntegerNumberProvider] for `UncachedInteger`
    @Override
    public @NonNull IntegerNumberProvider<UncachedInteger> provider() {
        return new IntegerNumberProvider<>() {

            @Override
            public UncachedInteger createValue(final long value) {
                if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                    throw new IllegalArgumentException("value (" + value + ") is not a valid Integer value");
                }
                return new UncachedInteger((int) value);
            }

            @Override
            public @NonNull UncachedInteger copyInstance(final @NonNull UncachedInteger other) {
                return new UncachedInteger(other);
            }

            @Override
            public @NonNull RecordingSupplier<UncachedInteger> uniqueInstanceSupplier() {
                return new RecordingSupplier<>() {
                    private int i = 0;
                    private final List<UncachedInteger> recorded = Collections.synchronizedList(new ArrayList<>());

                    @Override
                    public UncachedInteger get() {
                        UncachedInteger instance = new UncachedInteger(i++);
                        recorded.add(instance);
                        return instance;
                    }

                    @Override
                    public @NonNull List<UncachedInteger> recorded() {
                        return recorded;
                    }
                };
            }

            @Override
            public long maxIntegerValue() {
                return Integer.MAX_VALUE;
            }

            @Override
            public long minIntegerValue() {
                return Integer.MIN_VALUE;
            }
        };
    }

    /// Tests that the `valueOf()` method correctly wraps primitive integer values.
    @Test
    @DisplayName("The valueOf() method works.")
    public void valueOf_whenCalled_isSuccessful() {
        for (int i = -10; i < 10; i++) {
            UncachedInteger a = UncachedInteger.valueOf(i);
            assertEquals(i, a.intValue());
        }
    }

    /// Tests that the `valueOf()` method does not cache instances.
    ///
    /// Unlike [Integer#valueOf(int)], which caches values in the range -128 to 127,
    /// [UncachedInteger#valueOf(int)] must return a new instance every time it is called.
    @Test
    @DisplayName("The valueOf() method does not cache values")
    public void valueOf_whenCalled_doesNotCacheValues() {
        for (int i = -10; i < 10; i++) {
            UncachedInteger a = UncachedInteger.valueOf(i);
            UncachedInteger b = UncachedInteger.valueOf(i);
            assertNotSame(a, b);
        }
    }
}
