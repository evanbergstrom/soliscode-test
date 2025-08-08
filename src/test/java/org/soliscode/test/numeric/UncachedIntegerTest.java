package org.soliscode.test.numeric;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.numeric.IntegerContract;
import org.soliscode.test.provider.IntegerNumberProvider;
import org.soliscode.test.util.UncachedInteger;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

public class UncachedIntegerTest extends AbstractTest implements IntegerContract<UncachedInteger> {

    @Override
    public @NotNull IntegerNumberProvider<UncachedInteger> provider() {
        return new IntegerNumberProvider<UncachedInteger>() {

            @Override
            public UncachedInteger createValue(final int value) {
                return new UncachedInteger(value);
            }

            @Override
            public @NotNull UncachedInteger copyInstance(final @NotNull UncachedInteger other) {
                return new UncachedInteger(other);
            }

            @Override
            public @NotNull Supplier<UncachedInteger> uniqueInstanceSupplier() {
                return new Supplier<UncachedInteger>() {
                    private int i = 0;
                    @Override
                    public UncachedInteger get() {
                        return new UncachedInteger(i++);
                    }
                };
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
