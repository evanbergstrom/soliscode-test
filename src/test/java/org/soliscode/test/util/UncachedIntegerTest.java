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

public class UncachedIntegerTest extends AbstractTest implements IntegerContract<UncachedInteger> {

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

    @Test
    @DisplayName("The valueOf() method works.")
    public void testValueOfWorks() {
        for (int i = -10; i < 10; i++) {
            UncachedInteger a = UncachedInteger.valueOf(i);
            assertEquals(i, a.intValue());
        }
    }

    @Test
    @DisplayName("The valueOf() method does not cache values")
    public void testValueOfDoesNotCacheValues() {
        for (int i = -10; i < 10; i++) {
            UncachedInteger a = UncachedInteger.valueOf(i);
            UncachedInteger b = UncachedInteger.valueOf(i);
            assertNotSame(a, b);
        }
    }
}
